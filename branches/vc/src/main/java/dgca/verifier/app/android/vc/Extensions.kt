/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 25/03/2022, 23:16
 */

package dgca.verifier.app.android.vc

import android.content.Context
import android.util.Base64
import dgca.verifier.app.android.vc.domain.GetTrustListUseCase
import timber.log.Timber
import java.io.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Context.getStringFromJsonFile(fileId: Int): String {
    val inputStream: InputStream = resources.openRawResource(fileId)
    val writer: Writer = StringWriter()
    val buffer = CharArray(1024)
    try {
        val reader: Reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        var n: Int
        while (reader.read(buffer).also { n = it } != -1) {
            writer.write(buffer, 0, n)
        }
        return writer.toString()
    } catch (error: Exception) {
        Timber.e(error, "Error : ${error.printStackTrace()}")
    } finally {
        inputStream.close()
    }

    return ""
}

fun String.resolveHttpUrl(): String =
    if (endsWith(GetTrustListUseCase.TYPE_HTTP_SUFFIX).not() && endsWith(".json").not()) {
        "$this${GetTrustListUseCase.TYPE_HTTP_SUFFIX}"
    } else {
        this
    }

fun String.resolveDidUrl(): String =
    if (startsWith("did:web")) {
        val didUrl = drop(GetTrustListUseCase.DID.length).replace(":", "/")
        if (didUrl.contains("/")) {
            "https://${didUrl}/did.json"
        } else {
            "https://${didUrl}/.well-known/did.json"
        }
    } else {
        this
    }

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.formatWith(pattern: String): String =
    DateTimeFormatter.ofPattern(pattern).format(this)