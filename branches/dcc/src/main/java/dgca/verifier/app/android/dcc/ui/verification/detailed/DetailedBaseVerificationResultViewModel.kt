/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by osarapulov on 3/17/22, 2:02 PM
 */

package dgca.verifier.app.android.dcc.ui.verification.detailed

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.app.dcc.BuildConfig
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.dcc.Event
import dgca.verifier.app.android.dcc.anonymization.AnonymizationManager
import dgca.verifier.app.android.dcc.data.local.Preferences
import dgca.verifier.app.android.dcc.model.CertificateModel
import dgca.verifier.app.android.dcc.settings.debug.mode.DebugModeState
import dgca.verifier.app.android.dcc.ui.verification.detailed.qr.QrCodeConverter
import dgca.verifier.app.android.dcc.ui.verification.model.DebugData
import dgca.verifier.app.android.dcc.utils.sha256
import dgca.verifier.app.decoder.cbor.CborService
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

private const val BUFFER = 1024
private const val QR_CODE_SIZE = 400

@HiltViewModel
class DetailedBaseVerificationResultViewModel @Inject constructor(
    private val qrCodeConverter: QrCodeConverter,
    private val anonymizationManager: AnonymizationManager,
    private val coseService: CoseService,
    private val cborService: CborService,
    private val preferences: Preferences
) : ViewModel() {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    private val _event = MutableLiveData<Event<DetailedViewEvent>>()
    val event: LiveData<Event<DetailedViewEvent>> = _event

    @Suppress("BlockingMethodInNonBlockingContext")
    fun onShareClick(
        cachePath: String,
        certificateModel: CertificateModel?,
        hcert: String?,
        debugData: DebugData?
    ) {
        if (certificateModel == null || hcert == null || debugData == null) {
            return
        }

        _inProgress.value = true

        viewModelScope.launch {
            var zipPath = ""
            withContext(Dispatchers.IO) {
                try {
                    zipPath = generateZip(cachePath, certificateModel, debugData)
                } catch (ex: Exception) {
                    Timber.w(ex, "Exception during zip creation")
                }
            }
            _inProgress.value = false
            _event.value = Event(DetailedViewEvent.OnZipCreated(zipPath))
        }
    }

    @Throws(IOException::class)
    private fun generateZip(
        cachePath: String,
        certificateModel: CertificateModel,
        debugData: DebugData
    ): String {
        val debugPolicyLevel =
            (preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF)
        val cose = debugData.cose
        val cbor = debugData.cbor
        val qrCode = debugData.qrCode

        val version = generateVersionFile(cachePath)
        val readme = generateReadmeFile(cachePath)
        val payloadShaBin = generatePayloadShaBin(cachePath, cbor)
        val payloadShaTxt = generatePayloadShaTxt(cachePath, cbor)
        val qrBase64 = generateQrBase64(cachePath, cose, debugPolicyLevel)
        val payload = generatePayloadJson(cachePath, certificateModel, debugPolicyLevel)

        // Base list of files
        val list = mutableListOf(version, readme, payloadShaBin, payloadShaTxt, qrBase64, payload)

        //  L2/L3 Section
        if (debugPolicyLevel == DebugModeState.LEVEL_2 || debugPolicyLevel == DebugModeState.LEVEL_3) {
            val qrShaBin = generateQrShaBin(cachePath, qrCode)
            list.add(qrShaBin)

            val qrShaTxt = generateQrShaTxt(cachePath, qrCode)
            list.add(qrShaTxt)
        }

        if (debugPolicyLevel == DebugModeState.LEVEL_3) {
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

        return zip(cachePath, list)
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
    private fun generatePayloadShaBin(cachePath: String, cbor: ByteArray?): File {
        val payload = cbor?.let { cborService.getPayload(it) } ?: byteArrayOf()
        return createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_SHA_BIN), payload.sha256())
    }

    @Throws(IOException::class)
    private fun generatePayloadShaTxt(cachePath: String, cbor: ByteArray?): File {
        val payload = cbor?.let { cborService.getPayload(it) } ?: byteArrayOf()
        return createAndWriteToFile(
            cachePath.plusFile(Files.PAYLOAD_SHA_TXT),
            "${payload.toHexString().sha256()}\n"
        )
    }

    @Throws(IOException::class)
    private fun generateQrBase64(cachePath: String, cose: ByteArray?, state: DebugModeState): File {
        val coseByteArray = if (state == DebugModeState.LEVEL_3) {
            cose
        } else {
            cose?.let { coseService.anonymizeCose(it) }
        }

        return createAndWriteToFile(
            cachePath.plusFile(Files.QR_BASE_64),
            "${coseByteArray?.toBase64()}"
        )
    }

    @Throws(IOException::class)
    private fun generatePayloadJson(
        cachePath: String,
        certificateModel: CertificateModel,
        state: DebugModeState
    ): File {
        val result = anonymizationManager.anonymizeDcc(certificateModel, state)
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
        createAndWriteToFile(
            cachePath.plusFile(Files.COSE_SHA_TXT),
            "${cose?.toHexString()?.sha256()}\n"
        )

    @Throws(IOException::class)
    private fun generateCoseBase64(cachePath: String, cose: ByteArray?): File =
        createAndWriteToFile(cachePath.plusFile(Files.COSE_BASE64), "${cose?.toBase64()}")

    @Throws(IOException::class)
    private fun generatePayloadBase64(cachePath: String, cbor: ByteArray?): File {
        val payload = cbor?.let { cborService.getPayload(it) } ?: byteArrayOf()
        return createAndWriteToFile(cachePath.plusFile(Files.PAYLOAD_BASE64), payload.toBase64())
    }

    @Throws(IOException::class)
    private fun zip(cachePath: String, files: List<File>): String {
        var origin: BufferedInputStream?

        val zipFile = File(cachePath, Files.ZIP.fileName)
        val dest = FileOutputStream(zipFile)
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

        return zipFile.path
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

sealed class DetailedViewEvent {
    data class OnZipCreated(val filePath: String) : DetailedViewEvent()
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
