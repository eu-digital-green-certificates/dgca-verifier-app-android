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
 *  Created by osarapulov on 3/17/22, 2:22 PM
 */

package dgca.verifier.app.android.dcc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dcc.app.revocation.data.RevocationPreferences
import dgca.verifier.app.android.dcc.data.local.AppDatabase
import dgca.verifier.app.android.dcc.data.local.Preferences
import dgca.verifier.app.android.dcc.data.local.model.Key
import dgca.verifier.app.android.dcc.data.remote.ApiService
import dgca.verifier.app.android.dcc.security.KeyStoreCryptor
import dgca.verifier.app.decoder.base64ToX509Certificate
import dgca.verifier.app.decoder.toBase64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.security.cert.Certificate
import javax.inject.Inject

class VerifierRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferences: Preferences,
    private val revocationPreferences: RevocationPreferences,
    private val db: AppDatabase,
    private val keyStoreCryptor: KeyStoreCryptor
) : BaseRepository(), VerifierRepository {

    private val validCertList = mutableListOf<String>()
    private val mutex = Mutex()
    private val lastSyncLiveData = MutableLiveData(preferences.lastKeysSyncTimeMillis)

    override suspend fun fetchCertificates(statusUrl: String, updateUrl: String): Boolean? {
        mutex.withLock {
            return execute {
                val response = apiService.getCertStatus(statusUrl)
                val body = response.body() ?: return@execute false
                validCertList.clear()
                validCertList.addAll(body)

                val resumeToken = preferences.resumeToken
                fetchCertificate(updateUrl, resumeToken)
                db.keyDao().deleteAllExcept(validCertList.toTypedArray())
                preferences.lastKeysSyncTimeMillis = System.currentTimeMillis()
                lastSyncLiveData.postValue(preferences.lastKeysSyncTimeMillis)
                return@execute true
            }
        }
    }

    override suspend fun getCertificatesBy(kid: String): List<Certificate> =
        db.keyDao().getByKid(kid).map {
            keyStoreCryptor.decrypt(it.key)?.base64ToX509Certificate()!!
        }

    override fun getLastPubKeysSyncTimeMillis(): LiveData<Long> {
        return lastSyncLiveData
    }

    override fun getLastRevocationSyncTimeMillis(): Long = revocationPreferences.lastRevocationSyncTimeMillis

    private suspend fun fetchCertificate(url: String, resumeToken: Long) {
        val tokenFormatted = if (resumeToken == -1L) "" else resumeToken.toString()
        val response = apiService.getCertUpdate(tokenFormatted, url)

        if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
            val headers = response.headers()
            val responseKid = headers[HEADER_KID]
            val newResumeToken = headers[HEADER_RESUME_TOKEN]
            val responseStr = response.body()?.stringSuspending() ?: return

            if (isKidValid(responseKid, responseStr)) {
                Timber.d("Cert KID verified")

                if (validCertList.contains(responseKid)) {
                    val key = Key(kid = responseKid!!, key = keyStoreCryptor.encrypt(responseStr)!!)
                    db.keyDao().insert(key)
                }

                preferences.resumeToken = resumeToken

                newResumeToken?.let {
                    val newToken = it.toLong()
                    fetchCertificate(url, newToken)
                }
            }
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
