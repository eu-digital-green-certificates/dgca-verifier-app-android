/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by mykhailo.nester on 28/12/2021, 21:55
 */

package dcc.app.revocation.mock

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockRequestInterceptor(private val context: Context) : Interceptor {

    companion object {
        private val JSON_MEDIA_TYPE = "application/json".toMediaTypeOrNull()
        private const val MOCK = "mock"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val header = request.header(MOCK)

        if (header != null) {
            val filename = request.url.pathSegments.last()
            val builder = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .code(200)
            if (filename == "lists") {
                builder.body(context.readFileFromAssets("mocks/$filename.json").toResponseBody(JSON_MEDIA_TYPE))
            } else if (filename == "partitions") {
                builder.body(getPartition().toResponseBody())
            }

            return builder.build()
        }

        return chain.proceed(request.newBuilder().removeHeader(MOCK).build())
    }
}

fun Context.readFileFromAssets(filePath: String): String {
    return resources.assets.open(filePath).bufferedReader().use {
        it.readText()
    }
}