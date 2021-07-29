/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 */

package it.ministerodellasalute.verificaC19.ui.main.verification

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.toBase64
import it.ministerodellasalute.verificaC19.data.VerifierRepository
import it.ministerodellasalute.verificaC19.model.CertificateModel
import it.ministerodellasalute.verificaC19.model.toCertificateModel
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.toBase64
import it.ministerodellasalute.verificaC19.data.local.Preferences
import it.ministerodellasalute.verificaC19.data.remote.model.Rule
import it.ministerodellasalute.verificaC19.di.DispatcherProvider
import it.ministerodellasalute.verificaC19.model.ValidationRulesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "VerificationViewModel"

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val cryptoService: CryptoService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val verifierRepository: VerifierRepository,
    private val preferences: Preferences,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _verificationResult = MutableLiveData<VerificationResult>()
    val verificationResult: LiveData<VerificationResult> = _verificationResult

    private val _certificate = MutableLiveData<CertificateModel?>()
    val certificate: LiveData<CertificateModel?> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun init(qrCodeText: String) {
        decode(qrCodeText)
    }

    @SuppressLint("SetTextI18n")
    fun decode(code: String) {
        viewModelScope.launch {
            _inProgress.value = true
            var greenCertificate: GreenCertificate? = null
            val verificationResult = VerificationResult()

            withContext(dispatcherProvider.getIO()) {
                val plainInput = prefixValidationService.decode(code, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose: ByteArray? = compressorService.decode(compressedCose, verificationResult)
                if (cose == null) {
                    Log.d(TAG, "Verification failed: Too many bytes read")
                    return@withContext
                }

                val coseData = coseService.decode(cose, verificationResult)
                if (coseData == null) {
                    Log.d(TAG, "Verification failed: COSE not decoded")
                    return@withContext
                }

                val kid = coseData.kid
                if (kid == null) {
                    Log.d(TAG, "Verification failed: cannot extract kid from COSE")
                    return@withContext

                }

                schemaValidator.validate(coseData.cbor, verificationResult)
                greenCertificate = cborService.decode(coseData.cbor, verificationResult)

//                // Load from API for now. Replace with cache logic.
                val certificate = verifierRepository.getCertificate(kid.toBase64())

                if (certificate == null) {
                    Log.d(TAG, "Verification failed: failed to load certificate")
                    return@withContext
                }
                cryptoService.validate(cose, certificate, verificationResult)
            }

            _inProgress.value = false
            _certificate.value = greenCertificate?.toCertificateModel()
            _verificationResult.value = verificationResult
        }
    }

    private fun getValidationRules():Array<Rule>{
        val jsonString = preferences.validationRulesJson
        return Gson().fromJson(jsonString, Array<Rule>::class.java)
    }

    fun getRecoveryCertStartDay(): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.RECOVERY_CERT_START_DAY.value}?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getRecoveryCertEndDay(): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.RECOVERY_CERT_END_DAY.value}?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getRapidTestStartHour(): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.RAPID_TEST_START_HOUR.value}?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getRapidTestEndHour(): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.RAPID_TEST_END_HOUR.value}?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getVaccineStartDayNotComplete(vaccinType: String): String{

        return getValidationRules().find { it.name == ValidationRulesEnum.VACCINE_START_DAY_NOT_COMPLETE.value && it.type == vaccinType }?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getVaccineEndDayNotComplete(vaccinType: String): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.VACCINE_END_DAY_NOT_COMPLETE.value && it.type == vaccinType }?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getVaccineStartDayComplete(vaccinType: String): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.VACCINE_START_DAY_COMPLETE.value && it.type == vaccinType }?.let {
            it.value
        } ?: run {
            ""
        }
    }

    fun getVaccineEndDayComplete(vaccinType: String): String{
        return getValidationRules().find { it.name == ValidationRulesEnum.VACCINE_END_DAY_COMPLETE.value && it.type == vaccinType}?.let {
            it.value
        } ?: run {
            ""
        }
    }
}