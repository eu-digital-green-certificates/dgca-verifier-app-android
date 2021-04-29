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
 *  Created by mykhailo.nester on 4/24/21 2:16 PM
 */

package dgca.verifier.app.android.data

import android.util.Log
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.android.data.remote.ApiService
import dgca.verifier.app.decoder.chain.base64ToX509Certificate
import dgca.verifier.app.decoder.chain.toBase64
import java.net.HttpURLConnection
import java.security.MessageDigest
import javax.inject.Inject

class VerifierRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferences: Preferences
) : BaseRepository(), VerifierRepository {

    private val validCertList = mutableListOf<String>()

    override suspend fun fetchCertificates() {
        execute {
            val response = apiService.getCertStatus()
            val body = response.body() ?: return@execute
            validCertList.clear()
            validCertList.addAll(body)

            val resumeToken = preferences.resumeToken
            fetchCertificate(resumeToken)
        }
    }

    private suspend fun fetchCertificate(resumeToken: Long) {
        val tokenFormatted = if (resumeToken == -1L) "" else resumeToken.toString()
        val response = apiService.getCertUpdate(tokenFormatted)

        if (!response.isSuccessful || response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
            Log.i("VerifierRepository", "No content")
            return
        }

        val headers = response.headers()
        val responseKid = headers[HEADER_KID]
        val newResumeToken = headers[HEADER_RESUME_TOKEN]
        val responseStr = response.body()?.stringSuspending() ?: return

        if (validCertList.contains(responseKid) && isKidValid(responseKid, responseStr)) {
            // TODO: store in storage
//            LocalData.add(encodedPublicKey: responseStr)
            Log.i(VerifierRepositoryImpl::class.java.simpleName, "Cert KID verified")
        }

        newResumeToken?.let {
            val newToken = it.toLong()
            preferences.resumeToken = newToken
            fetchCertificate(newToken)
        }
    }

    private fun isKidValid(responseKid: String?, responseStr: String): Boolean {
        if (responseKid == null) return false

        val cert = responseStr.base64ToX509Certificate() ?: return false
        val certKid = MessageDigest.getInstance("SHA-256")
            .digest(cert.encoded)
            .copyOf(8)
            .toBase64()

        return responseKid == certKid
    }

    companion object {

        const val HEADER_KID = "x-kid"
        const val HEADER_RESUME_TOKEN = "x-resume-token"
    }
}

