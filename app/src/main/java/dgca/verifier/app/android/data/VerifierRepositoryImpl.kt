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
import dgca.verifier.app.android.data.remote.ApiService
import dgca.verifier.app.decoder.chain.base64ToX509Certificate
import dgca.verifier.app.decoder.chain.toBase64
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.inject.Inject

class VerifierRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), VerifierRepository {

    override suspend fun getCertificate(key: String): Certificate? {
        return execute {
            val response = apiService.getCertificates(key)
            response.body()?.byteStream()?.let {
                return@execute CertificateFactory.getInstance("X.509").generateCertificate(it)
            }
        }
    }

    override suspend fun getCertUpdate() {
        execute {
            val response = apiService.getCertUpdate()
            val headers = response.headers()
            val responseKid = headers["x-kid"]
            val newResumeToken = headers["x-resume-token"]
            val responseStr = response.body()?.stringSuspending() ?: return@execute
            val cert = responseStr.base64ToX509Certificate() ?: return@execute

            val certKid = MessageDigest.getInstance("SHA-256")
                .digest(cert.encoded)
                .copyOfRange(0, 8)
                .toBase64()

            if (responseKid != certKid) {
                return@execute
            }

            Log.d(VerifierRepositoryImpl::class.java.simpleName, "Cert KID verified")

            // TODO: store in storage
//            let kid = KID.from(responseStr)
//            let kidStr = KID.string(from: kid)
//            if kidStr != responseKid {
//                return
//            }
//            LocalData.add(encodedPublicKey: responseStr)
//            LocalData.set(resumeToken: newResumeToken)

        }
    }

    override suspend fun getValidCertIds() {
        execute {
            val result = apiService.getCertStatus()
            println(result)

            // TODO: check local storage and remove all that now match
        }
    }
}

