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
 *  Created by mykhailo.nester on 4/24/21 2:54 PM
 */

package dgca.verifier.app.android.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.data.VerifierRepository
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.toCertificateModel
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.TestVerificationResult
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.ExternalParameter
import dgca.verifier.app.engine.JsonLogicValidator
import dgca.verifier.app.engine.data.source.RulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

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
    private val jsonLogicValidator: JsonLogicValidator,
    private val rulesRepository: RulesRepository
) : ViewModel() {

    companion object {
        private const val ENGINE_VERSION = "1.0.0"
    }

    private val _verificationResult = MutableLiveData<VerificationResult?>()
    val verificationResult: LiveData<VerificationResult?> = _verificationResult

    private val _verificationError = MutableLiveData<VerificationError>()
    val verificationError: LiveData<VerificationError> = _verificationError

    private val _certificate = MutableLiveData<Pair<String, CertificateModel>?>()
    val certificate: LiveData<Pair<String, CertificateModel>?> = _certificate

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    fun init(qrCodeText: String) {
        decode(qrCodeText)
    }

    private fun decode(code: String) {
        viewModelScope.launch {
            _inProgress.value = true
            var greenCertificate: Pair<String, GreenCertificate>? = null
            val verificationResult = VerificationResult()
            var noPublicKeysFound = true

            var isApplicableCode = false
            withContext(Dispatchers.IO) {
                val plainInput = prefixValidationService.decode(code, verificationResult)
                val compressedCose = base45Service.decode(plainInput, verificationResult)
                val cose = compressorService.decode(compressedCose, verificationResult)

                val coseData = coseService.decode(cose, verificationResult)
                if (coseData == null) {
                    Timber.d("Verification failed: COSE not decoded")
                    return@withContext
                }

                val kid = coseData.kid
                if (kid == null) {
                    Timber.d("Verification failed: cannot extract kid from COSE")
                    return@withContext
                }

                isApplicableCode = true

                schemaValidator.validate(coseData.cbor, verificationResult)
                greenCertificate = cborService.decodeData(coseData.cbor, verificationResult)
                validateCertData(greenCertificate?.second, verificationResult)

                val certificates = verifierRepository.getCertificatesBy(kid.toBase64())
                if (certificates.isEmpty()) {
                    Timber.d("Verification failed: failed to load certificate")
                    return@withContext
                }
                noPublicKeysFound = false
                certificates.forEach { innerCertificate ->
                    cryptoService.validate(cose, innerCertificate, verificationResult)
                    if (verificationResult.coseVerified) {
                        return@forEach
                    }
                }
            }

            verificationResult.fetchError(noPublicKeysFound)
                ?.apply { _verificationError.value = this }

            _inProgress.value = false
            _verificationResult.value = if (isApplicableCode) verificationResult else null
            _certificate.value =
                greenCertificate?.let { Pair(it.first, it.second.toCertificateModel()) }
        }
    }

    private fun validateCertData(
        certificate: GreenCertificate?,
        verificationResult: VerificationResult
    ) {
        certificate?.tests?.let {
            if (it.isNotEmpty()) {
                val test = it.first()
                verificationResult.testVerification =
                    TestVerificationResult(test.isResultNegative(), test.isDateInThePast())
            }
        }
    }

    fun validate(countryIsoCode: String) {
        val json = certificate.value?.first
        if (json?.isNotEmpty() == true) {
            viewModelScope.launch {
                _inProgress.value = true
                withContext(Dispatchers.IO) {
                    val rules = rulesRepository.getRulesBy(countryIsoCode)
                    val engine = DefaultCertLogicEngine(jsonLogicValidator, ENGINE_VERSION, rules)
                    val results = engine.validate(ExternalParameter(LocalDate.now(), emptyMap(), countryIsoCode, LocalDate.now(), LocalDate.now()), json)
                    return@withContext
                }
                _inProgress.value = false
            }
        }
    }
}