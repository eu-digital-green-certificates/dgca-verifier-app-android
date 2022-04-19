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
 *  Created by mykhailo.nester on 23/03/2022, 22:34
 */

package dgca.verifier.app.android.vc.data

import com.google.gson.Gson
import dgca.verifier.app.android.vc.containsServerError
import dgca.verifier.app.android.vc.data.local.JwkDao
import dgca.verifier.app.android.vc.data.local.JwkLocal
import dgca.verifier.app.android.vc.data.local.VcPreferences
import dgca.verifier.app.android.vc.data.remote.VcApiService
import dgca.verifier.app.android.vc.data.remote.model.Jwk
import dgca.verifier.app.android.vc.data.remote.model.SignerCertificate
import dgca.verifier.app.android.vc.data.remote.model.VerificationMethod
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject

class VcRepositoryImpl @Inject constructor(
    private val preferences: VcPreferences,
    private val apiService: VcApiService,
    private val dao: JwkDao
) : VcRepository {

    override suspend fun loadTrustList(): List<SignerCertificate> {
        val eTag = preferences.eTag ?: ""

        val response = apiService.fetchTrustList(eTag)

        if (response.containsServerError()) {
            throw HttpException(response)
        }

        return if (response.code() == HttpURLConnection.HTTP_OK) {
            preferences.eTag = response.headers()["eTag"]?.replace("\"", "")
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    override suspend fun resolveIssuer(url: String): List<Jwk> =
        try {
            apiService.resolveIssuer(url).body()?.keys ?: emptyList()
        } catch (ex: IOException) {
            Timber.e(ex, "Failed to fetch jwk by http url issuer")
            emptyList()
        }

    override suspend fun resolveIssuerByDid(fullUrl: String): List<VerificationMethod> =
        try {
            apiService.resolveIssuerByDid(fullUrl).body()?.verificationMethod ?: emptyList()
        } catch (ex: IOException) {
            Timber.e(ex, "Failed to fetch jwk by did issuer")
            emptyList()
        }

    override suspend fun saveJWKs(result: List<Jwk>) {
        val localJwkList = result.map { JwkLocal(it.kid, Gson().toJson(it)) }
        dao.save(localJwkList)
    }

    override suspend fun getIssuerJWKsByKid(kid: String): List<Jwk> =
        try {
            dao.getIssuer(kid).map { Gson().fromJson(it.jwk, Jwk::class.java) }
        } catch (ex: Exception) {
            Timber.e(ex, "Cannot parse local jwk list")
            emptyList()
        }

    override suspend fun removeOutdated() {
        try {
            dao.deleteAll()
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to clear db")
        }
    }
}