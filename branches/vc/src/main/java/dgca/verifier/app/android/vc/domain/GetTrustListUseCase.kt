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
 *  Created by mykhailo.nester on 26/04/2022, 23:47
 */

package dgca.verifier.app.android.vc.domain

import dgca.verifier.app.android.vc.data.VcRepository
import dgca.verifier.app.android.vc.data.local.VcPreferences
import dgca.verifier.app.android.vc.data.remote.model.IssuerType
import dgca.verifier.app.android.vc.di.VerifiableCredentials
import dgca.verifier.app.android.vc.resolveDidUrl
import dgca.verifier.app.android.vc.resolveHttpUrl
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetTrustListUseCase @Inject constructor(
    private val repository: VcRepository,
    private val preferences: VcPreferences,
    @VerifiableCredentials dispatcher: CoroutineDispatcher,
    @VerifiableCredentials errorHandler: ErrorHandler,
) : BaseUseCase<Unit, Any>(dispatcher, errorHandler) {

    override suspend fun invoke(params: Any) {
        val certificates = repository.loadTrustList()
        certificates
            .filter { it.keyStorageType == KEY_STORAGE_TYPE }
            .forEach {
                when (it.type) {
                    IssuerType.HTTP -> resolveIssuer(it.url)
                    IssuerType.DID -> resolveDid(it.url)
                    else -> {}
                }
            }

        preferences.trustListSyncTimeMillis = System.currentTimeMillis()
    }

    private suspend fun resolveIssuer(url: String) {
        val resolvedUrl = url.resolveHttpUrl()
        val result = repository.resolveIssuer(resolvedUrl)
        if (result.isNotEmpty()) {
            repository.saveJWKs(result, resolvedUrl)
        }
    }

    private suspend fun resolveDid(url: String) {
        val resolvedUrl = url.resolveDidUrl()
        val result = repository.resolveIssuerByDid(resolvedUrl)
        if (result.isNotEmpty()) {
            repository.saveJWKs(result.map { it.publicKeyJwk }, resolvedUrl)
        }
    }

    companion object {
        const val KEY_STORAGE_TYPE = "JWKS"
        const val TYPE_HTTP_SUFFIX = "/.well-known/jwks.json"
        const val DID = "did:web:"
    }
}