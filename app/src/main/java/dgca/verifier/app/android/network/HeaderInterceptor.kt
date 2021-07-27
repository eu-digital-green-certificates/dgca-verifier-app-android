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
 *  Created by mykhailo.nester on 4/24/21 1:53 PM
 */

package dgca.verifier.app.android.network

import android.os.Build
import dgca.verifier.app.android.BackportUtils
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.security.MessageDigest

class HeaderInterceptor : Interceptor {

    private val userAgent = "DGCA verifier Android ${Build.VERSION.SDK_INT}, ${Build.MODEL};"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addHeadersToRequest(chain.request())

        val response = chain.proceed(request)
        return if (request.isRuleRequest()) {
            response.toRuleResponse(request.url.pathSegments[2])
        } else {
            response
        }
    }

    private fun addHeadersToRequest(original: Request): Request {
        val requestBuilder = original.newBuilder()
            .header("User-Agent", userAgent)

        return requestBuilder.build()
    }
}

private fun Request.isRuleRequest(): Boolean =
    this.url.pathSegments.size == 3 && this.url.pathSegments.contains("rules")

private fun Response.toRuleResponse(expectedSha256: String): Response = if (this.isSuccessful) {
    val newResponse = this.newBuilder()
    val responseString = this.newBuilder().build().body?.string()
    val contentType = this.header("Content-Type")
    val sha256 = responseString!!.sha256()
    if (sha256 != expectedSha256) newResponse.code(HTTP_BAD_REQUEST)
    newResponse.body(responseString.toResponseBody(contentType!!.toMediaType()))
    newResponse.build()
} else {
    this
}

private fun String.sha256(): String {
    val sb = StringBuilder()
    try {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        digest.update(this.toByteArray())
        val byteData: ByteArray = digest.digest()
        for (x in byteData) {
            val str = Integer.toHexString(BackportUtils.byteToUnsignedInt(x))
            if (str.length < 2) {
                sb.append('0')
            }
            sb.append(str)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return sb.toString()
}