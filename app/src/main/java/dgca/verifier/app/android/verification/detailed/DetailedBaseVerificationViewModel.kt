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
 *  Created by osarapulov on 8/31/21 10:49 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.BuildConfig
import dgca.verifier.app.android.anonymization.AnonymizationManager
import dgca.verifier.app.android.anonymization.PolicyLevel
import dgca.verifier.app.android.data.VerifierRepository
import dgca.verifier.app.android.utils.sha256
import dgca.verifier.app.android.verification.BaseVerificationViewModel
import dgca.verifier.app.android.verification.DecodeResult
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.decoder.toHexString
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

enum class VerificationComponent { TECHNICAL_VERIFICATION, ISSUER_INVALIDATION, DESTINATION_INVALIDATION, TRAVELLER_ACCEPTANCE }

enum class VerificationComponentState { PASSED, FAILED, OPEN }

enum class VerificationResult { VALID, INVALID, LIMITED_VALIDITY }

fun Map<VerificationComponent, VerificationComponentState>.toVerificationResult(): VerificationResult =
    when {
        this[VerificationComponent.TECHNICAL_VERIFICATION] != VerificationComponentState.PASSED || this[VerificationComponent.TRAVELLER_ACCEPTANCE] != VerificationComponentState.PASSED -> VerificationResult.INVALID
        this[VerificationComponent.ISSUER_INVALIDATION] == VerificationComponentState.PASSED && this[VerificationComponent.DESTINATION_INVALIDATION] == VerificationComponentState.PASSED -> VerificationResult.VALID
        else -> VerificationResult.LIMITED_VALIDITY
    }


private const val BUFFER = 1024

@HiltViewModel
class DetailedBaseVerificationViewModel @Inject constructor(
    prefixValidationService: PrefixValidationService,
    base45Service: Base45Service,
    compressorService: CompressorService,
    cryptoService: CryptoService,
    coseService: CoseService,
    schemaValidator: SchemaValidator,
    cborService: CborService,
    verifierRepository: VerifierRepository,
    engine: CertLogicEngine,
    getRulesUseCase: GetRulesUseCase,
    valueSetsRepository: ValueSetsRepository,
    private val anonymizationManager: AnonymizationManager
) : BaseVerificationViewModel(
    prefixValidationService,
    base45Service,
    compressorService,
    cryptoService,
    coseService,
    schemaValidator,
    cborService,
    verifierRepository,
    engine,
    getRulesUseCase,
    valueSetsRepository
) {
    private val _detailedVerificationResult = MutableLiveData<DetailedVerificationResult>()
    val detailedVerificationResult: LiveData<DetailedVerificationResult> =
        _detailedVerificationResult

    private val policyLevel = PolicyLevel.L1

    override fun handleDecodeResult(decodeResult: DecodeResult) {
        _detailedVerificationResult.value = decodeResult.toDetailedVerificationResult()
    }

    private fun DecodeResult.toDetailedVerificationResult(): DetailedVerificationResult {
        return DetailedVerificationResult(
            this.verificationData.certificateModel,
            this.verificationError
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun onShareClick(context: Context, qrCodeText: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (certificateModel == null) {
                    return@withContext
                }

                val result = anonymizationManager.anonymizeDcc(certificateModel!!, policyLevel)

                val json = Gson().toJson(result)
                Timber.d("Result: $json")

                val version = createAndWriteToFile("${context.cacheDir.path}/VERSION.txt", "1.00\n")
                val readme = createAndWriteToFile(
                    "${context.cacheDir.path}/README.txt",
                    "Timestamp: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}\n" +
                            "App Version name: ${BuildConfig.VERSION_NAME}\n" +
                            "App Version code: ${BuildConfig.VERSION_CODE}\n"
                )

                val payload = createAndWriteToFile("${context.cacheDir.path}/payload.json", json)

                val payloadShaBin = File("${context.cacheDir.path}/payload-sha.bin")
                if (payloadShaBin.exists()) {
                    payloadShaBin.delete()
                }
                payloadShaBin.createNewFile()
                coseData?.cbor?.sha256()?.let { payloadShaBin.writeBytes(it) }

                val payloadShaTxt = createAndWriteToFile(
                    "${context.cacheDir.path}/payload-sha.txt",
                    coseData?.cbor?.toHexString()?.sha256() + "\n"
                )


                val coseByteArray = if (policyLevel == PolicyLevel.L3) {
                    cose
                } else {
                    anonymizeCose
                }

                val qrBase64 = createAndWriteToFile("${context.cacheDir.path}/QR.base64", coseByteArray?.toBase64() ?: "")

                // Base list of files
                val list = mutableListOf(version, readme, payload, payloadShaBin, payloadShaTxt, qrBase64)

                //  L2/L3 Section
                if (policyLevel == PolicyLevel.L2 || policyLevel == PolicyLevel.L3) {
                    val qrShaBin = File("${context.cacheDir.path}/QR-sha.bin")
                    if (qrShaBin.exists()) {
                        qrShaBin.delete()
                    }
                    qrShaBin.createNewFile()
                    qrCodeText.toByteArray().sha256()?.let { qrShaBin.writeBytes(it) }
                    list.add(qrShaBin)

                    val qrShaTxt = createAndWriteToFile("${context.cacheDir.path}/QR-sha.txt", "${qrCodeText.sha256()}\n")
                    list.add(qrShaTxt)
                }

                if (policyLevel == PolicyLevel.L3) {

                }


                zip(context, list, "EmergencyModeZip.zip")
            }

            // TODO: return path to zip file. Share via email
        }
    }

    private fun createAndWriteToFile(path: String, content: String): File {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }

        file.createNewFile()
        file.bufferedWriter().use { out ->
            out.write(content)
        }

        return file
    }

    private fun zip(context: Context, files: List<File>, zipFileName: String) {
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(File(context.cacheDir, zipFileName))
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(BUFFER)
            for (i in files.indices) {
                Timber.v("Compress: Adding: ${files[i]}")
                val fi = FileInputStream(files[i])
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(files[i].name)
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}