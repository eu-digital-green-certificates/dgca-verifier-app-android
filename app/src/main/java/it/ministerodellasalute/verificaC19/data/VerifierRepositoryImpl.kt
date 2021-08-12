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

package it.ministerodellasalute.verificaC19.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dgca.verifier.app.decoder.base64ToX509Certificate
import dgca.verifier.app.decoder.toBase64
import it.ministerodellasalute.verificaC19.data.local.AppDatabase
import it.ministerodellasalute.verificaC19.data.local.Key
import it.ministerodellasalute.verificaC19.data.local.Preferences
import it.ministerodellasalute.verificaC19.data.remote.ApiService
import it.ministerodellasalute.verificaC19.di.DispatcherProvider
import it.ministerodellasalute.verificaC19.security.KeyStoreCryptor
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.security.cert.Certificate
import javax.inject.Inject


class VerifierRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferences: Preferences,
    private val db: AppDatabase,
    private val keyStoreCryptor: KeyStoreCryptor,
    private val dispatcherProvider: DispatcherProvider
) : BaseRepository(dispatcherProvider), VerifierRepository {

    private val validCertList = mutableListOf<String>()
    private val fetchStatus: MutableLiveData<Boolean> = MutableLiveData()

    override suspend fun syncData(): Boolean? {
        return execute {
            fetchStatus.postValue(true)

            fetchValidationRules()

            if (fetchCertificates() == false) {
                fetchStatus.postValue(false)
                return@execute false
            }

            fetchStatus.postValue(false)
            return@execute true
        }
    }

    private suspend fun fetchValidationRules() {
        val response = apiService.getValidationRules()
        val body = response.body() ?: run {
            return
        }
        preferences.validationRulesJson = body.stringSuspending(dispatcherProvider)
    }

    private suspend fun fetchCertificates(): Boolean? {
        return execute {

            val response = apiService.getCertStatus()
            val body = response.body() ?: run {
                return@execute false
            }
            validCertList.clear()
            validCertList.addAll(body)

            if (body.isEmpty()) {
                preferences.resumeToken = -1L
            }

            val resumeToken = preferences.resumeToken
            fetchCertificate(resumeToken)
            db.keyDao().deleteAllExcept(validCertList.toTypedArray())

            preferences.dateLastFetch = System.currentTimeMillis()

            return@execute true
        }
    }

    override suspend fun getCertificate(kid: String): Certificate? {
        val key = db.keyDao().getById(kid)
        return if (key != null) keyStoreCryptor.decrypt(key.key)!!
            .base64ToX509Certificate() else null
    }

    override fun getCertificateFetchStatus(): LiveData<Boolean> {
        return fetchStatus
    }

    private suspend fun fetchCertificate(resumeToken: Long) {
        val tokenFormatted = if (resumeToken == -1L) "" else resumeToken.toString()
        val response = apiService.getCertUpdate(tokenFormatted)

        if (response.isSuccessful && response.code() == HttpURLConnection.HTTP_OK) {
            val headers = response.headers()
            val responseKid = headers[HEADER_KID]
            val newResumeToken = headers[HEADER_RESUME_TOKEN]
            val responseStr = response.body()?.stringSuspending(dispatcherProvider) ?: return

            if (validCertList.contains(responseKid)) {
                Log.i(VerifierRepositoryImpl::class.java.simpleName, "Cert KID verified")
                val key = Key(kid = responseKid!!, key = keyStoreCryptor.encrypt(responseStr)!!)
                db.keyDao().insert(key)

                preferences.resumeToken = resumeToken

                newResumeToken?.let {
                    val newToken = it.toLong()
                    fetchCertificate(newToken)
                }
            }
        }
    }

    companion object {

        const val HEADER_KID = "x-kid"
        const val HEADER_RESUME_TOKEN = "x-resume-token"
    }

}

