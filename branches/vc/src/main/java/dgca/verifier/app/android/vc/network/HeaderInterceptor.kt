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
 *  Created by mykhailo.nester on 23/03/2022, 22:33
 */

package dgca.verifier.app.android.vc.network

import android.os.Build
import dgca.verifier.app.android.vc.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HeaderInterceptor : Interceptor {

    private val userAgent =
        "DCC verifier vc: ${BuildConfig.VERSION_NAME}, Android: ${Build.VERSION.SDK_INT}, Model: ${Build.MODEL};"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = addHeadersToRequest(chain.request())
        return chain.proceed(request)
    }

    private fun addHeadersToRequest(original: Request): Request {
        val requestBuilder = original.newBuilder().header("User-Agent", userAgent)
        return requestBuilder.build()
    }
}