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

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.BuildConfig
import dgca.verifier.app.android.anonymization.AnonymizationManager
import dgca.verifier.app.android.anonymization.PolicyLevel
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.utils.sha256
import dgca.verifier.app.android.verification.DebugData
import dgca.verifier.app.android.verification.StandardizedVerificationResultCategory
import dgca.verifier.app.android.verification.detailed.qr.QrCodeConverter
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.decoder.toHexString
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

fun Map<VerificationComponent, VerificationComponentState>.toVerificationResult(): StandardizedVerificationResultCategory =
    when {
        this[VerificationComponent.TECHNICAL_VERIFICATION] != VerificationComponentState.PASSED || this[VerificationComponent.TRAVELLER_ACCEPTANCE] != VerificationComponentState.PASSED -> StandardizedVerificationResultCategory.INVALID
        this[VerificationComponent.ISSUER_INVALIDATION] == VerificationComponentState.PASSED && this[VerificationComponent.DESTINATION_INVALIDATION] == VerificationComponentState.PASSED -> StandardizedVerificationResultCategory.VALID
        else -> StandardizedVerificationResultCategory.LIMITED_VALIDITY
    }

private const val BUFFER = 1024
private const val QR_CODE_SIZE = 400

@HiltViewModel
class DetailedBaseVerificationResultViewModel @Inject constructor(
    private val qrCodeConverter: QrCodeConverter,
    private val anonymizationManager: AnonymizationManager,
    private val coseService: CoseService
) : ViewModel() {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    var policyLevel = PolicyLevel.L3

    @Suppress("BlockingMethodInNonBlockingContext")
    fun onShareClick(cachePath: String, certificateModel: CertificateModel?, hcert: String?, debugData: DebugData?) {
        if (certificateModel == null || hcert == null || debugData == null) {
            return
        }

        _inProgress.value = true

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    generateZip(cachePath, certificateModel, debugData)
                } catch (ex: Exception) {
                    Timber.w(ex, "Exception during zip creation")
                }
            }

            _inProgress.value = false

            // TODO: return path to zip file. Share via email
        }
    }

    @Throws(IOException::class)
    private fun generateZip(cachePath: String, certificateModel: CertificateModel, debugData: DebugData) {
        val cose = debugData.cose
        val cbor = debugData.cbor
        val qrCode = debugData.qrCode

        val version = generateVersionFile(cachePath)
        val readme = generateReadmeFile(cachePath)
        val payloadShaBin = generatePayloadShaBin(cachePath, cbor)
        val payloadShaTxt = generatePayloadShaTxt(cachePath, cbor)
        val qrBase64 = generateQrBase64(cachePath, cose, policyLevel)
        val payload = generatePayloadJson(cachePath, certificateModel, policyLevel)

        // Base list of files
        val list = mutableListOf(version, readme, payloadShaBin, payloadShaTxt, qrBase64, payload)

        //  L2/L3 Section
        if (policyLevel == PolicyLevel.L2 || policyLevel == PolicyLevel.L3) {
            val qrShaBin = generateQrShaBin(cachePath, qrCode)
            list.add(qrShaBin)

            val qrShaTxt = generateQrShaTxt(cachePath, qrCode)
            list.add(qrShaTxt)
        }

        if (policyLevel == PolicyLevel.L3) {
            val bitmap = qrCodeConverter.convertStringIntoQrCode(qrCode, QR_CODE_SIZE)
            val qrImageFile = bitmapToFile(cachePath, bitmap)
            list.add(qrImageFile)

            val qrTxt = generateQrTxt(cachePath, qrCode)
            list.add(qrTxt)

            val coseShaBin = generateCoseShaBin(cachePath, cose)
            list.add(coseShaBin)

            val coseShaTxt = generateCoseShaTxt(cachePath, cose)
            list.add(coseShaTxt)

            val coseBase64 = generateCoseBase64(cachePath, cose)
            list.add(coseBase64)

            val payloadBase64 = generatePayloadBase64(cachePath, cbor)
            list.add(payloadBase64)
        }

        zip(cachePath, list)
    }

    @Throws(IOException::class)
    private fun generateVersionFile(cachePath: String): File =
        createAndWriteToFile(cachePath.plusFile(Files.VERSION), "1.00\n")

    @Throws(IOException::class)
    private fun generateReadmeFile(cachePath: String): File =
        createAndWriteToFile(
            cachePath.plusFile(Files.README),
            "Timestamp: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}\n" +
                    "App Version name: ${BuildConfig.VERSION_NAME}\n" +
                    "App Version code: ${BuildConfig.VERSION_CODE}\n"
        )

    @Throws(IOException::class)
    private fun generatePayloadShaBin(cachePath: String, cbor: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_SHA_BIN), cbor?.sha256())

    @Throws(IOException::class)
    private fun generatePayloadShaTxt(cachePath: String, cbor: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_SHA_TXT), "${cbor?.toHexString()?.sha256()}\n")

    @Throws(IOException::class)
    private fun generateQrBase64(cachePath: String, cose: ByteArray?, policyLevel: PolicyLevel): File {
        val coseByteArray = if (policyLevel == PolicyLevel.L3) {
            cose
        } else {
            cose?.let { coseService.anonymizeCose(it) }
        }

        return createAndWriteToFile(cachePath.plusFile(Files.QR_BASE_64), "${coseByteArray?.toBase64()}")
    }

    @Throws(IOException::class)
    private fun generatePayloadJson(cachePath: String, certificateModel: CertificateModel, policyLevel: PolicyLevel): File {
        val result = anonymizationManager.anonymizeDcc(certificateModel, policyLevel)
        val json = ObjectMapper().writeValueAsString(result)
        return createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_JSON), json)
    }

    @Throws(IOException::class)
    private fun generateQrShaBin(cachePath: String, qrCode: String): File =
        createAndWriteToFile(cachePath.plusFile(Files.QR_SHA_BIN), qrCode.toByteArray().sha256())

    @Throws(IOException::class)
    private fun generateQrShaTxt(cachePath: String, qrCode: String): File =
        createAndWriteToFile(cachePath.plusFile(Files.QR_SHA_TXT), "${qrCode.sha256()}\n")

    @Throws(IOException::class)
    fun bitmapToFile(cachePath: String, bitmap: Bitmap): File {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData = bos.toByteArray()

        return createAndWriteToFile(cachePath.plusFile(Files.QR_PNG), bitmapData)
    }

    @Throws(IOException::class)
    private fun generateQrTxt(cachePath: String, qrCode: String): File =
        createAndWriteToFile(cachePath.plusFile(Files.QR_TXT), qrCode)

    @Throws(IOException::class)
    private fun generateCoseShaBin(cachePath: String, cose: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.COSE_SHA_BIN), cose?.sha256())

    @Throws(IOException::class)
    private fun generateCoseShaTxt(cachePath: String, cose: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.COSE_SHA_TXT), "${cose?.toHexString()?.sha256()}\n")

    @Throws(IOException::class)
    private fun generateCoseBase64(cachePath: String, cose: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.COSE_BASE64), "${cose?.toBase64()}")

    @Throws(IOException::class)
    private fun generatePayloadBase64(cachePath: String, cbor: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_BASE64), "${cbor?.toBase64()}")

    @Throws(IOException::class)
    private fun zip(cachePath: String, files: List<File>) {
        var origin: BufferedInputStream?
        val dest = FileOutputStream(File(cachePath, Files.ZIP.fileName))
        val out = ZipOutputStream(BufferedOutputStream(dest))
        val data = ByteArray(BUFFER)
        for (i in files.indices) {
            Timber.d("Compress: Adding: ${files[i]}")
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
    }

    @Throws(IOException::class)
    private fun createAndWriteToFile(path: String, content: String): File {
        val file = createNewFile(path)
        file.bufferedWriter().use { out ->
            out.write(content)
        }

        return file
    }

    @Throws(IOException::class)
    private fun createAndWriteToFile(path: String, content: ByteArray?): File {
        val file = createNewFile(path)
        content?.let { file.writeBytes(it) }

        return file
    }

    @Throws(IOException::class)
    private fun createNewFile(path: String): File {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        return file
    }
}

enum class Files(val fileName: String) {
    VERSION("VERSION.txt"),
    README("README.txt"),
    PAYLOAD_SHA_BIN("payload-sha.bin"),
    PAYLOAD_SHA_TXT("payload-sha.txt"),
    PAYLOAD_JSON("payload.json"),
    QR_BASE_64("QR.base64"),
    QR_SHA_BIN("QR-sha.bin"),
    QR_SHA_TXT("QR-sha.txt"),
    QR_PNG("QR.png"),
    QR_TXT("QR.txt"),
    COSE_SHA_BIN("cose-sha.bin"),
    COSE_SHA_TXT("cose-sha.txt"),
    COSE_BASE64("cose.base64"),
    PAYLOAD_BASE64("payload.base64"),
    ZIP("EmergencyMode.zip")
}

fun String.plusFile(file: Files): String = this + "/${file.fileName}"