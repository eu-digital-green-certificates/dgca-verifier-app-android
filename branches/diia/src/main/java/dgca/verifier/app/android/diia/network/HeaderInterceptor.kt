/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 2:49 PM
 */

package dgca.verifier.app.android.diia.network

import android.os.Build
import com.android.app.diia.BuildConfig
import dgca.verifier.app.android.diia.utils.sha256
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.HttpURLConnection.HTTP_BAD_REQUEST

class HeaderInterceptor : Interceptor {

    private val userAgent =
        "DGCA verifier: ${BuildConfig.VERSION_NAME}, Android: ${Build.VERSION.SDK_INT}, Model: ${Build.MODEL};"

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
