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

package dgca.verifier.app.android.vc.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dgca.verifier.app.android.vc.data.VcRepository
import dgca.verifier.app.android.vc.data.remote.model.IssuerType
import timber.log.Timber

@HiltWorker
class TrustListLoadingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val vcRepository: VcRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        Timber.d("Trust list loading start")
        return try {
//            TODO: add reload Button in setting to reload manually

            val certificates = vcRepository.loadTrustList()
            if (certificates.isNotEmpty()) {
                vcRepository.removeOutdated()
            }

            certificates
                .filter { it.keyStorageType == KEY_STORAGE_TYPE }
                .forEach {
                    when (it.type) {
                        IssuerType.HTTP -> resolveIssuer(it.url)
                        IssuerType.DID -> resolveDid(it.url)
                        else -> {}
                    }
                }

            Timber.d("Trust list loading success")
            Result.success()
        } catch (error: Throwable) {
            Timber.d(error, "Trust list loading error: $error")
            Result.retry()
        }
    }

    private suspend fun resolveIssuer(url: String) {
        var fullUrl = url
        if (url.endsWith(TYPE_HTTP_SUFFIX).not() && url.endsWith(".json").not()) {
            fullUrl = "$url$TYPE_HTTP_SUFFIX"
        }

        val result = vcRepository.resolveIssuer(fullUrl)
        if (result.isNotEmpty()) {
            vcRepository.saveJWKs(result)
        }
    }

    private suspend fun resolveDid(url: String) {
        var resolvedUrl = url
        if (url.startsWith("did:web")) {
            val didUrl = url.drop(DID.length).replace(":", "/")
            resolvedUrl = if (didUrl.contains("/")) {
                "https://${didUrl}/did.json"
            } else {
                "https://${didUrl}/.well-known/did.json"
            }
        }

        val result = vcRepository.resolveIssuerByDid(resolvedUrl)
        if (result.isNotEmpty()) {
            vcRepository.saveJWKs(result.map { it.publicKeyJwk })
        }
    }

    companion object {
        const val KEY_STORAGE_TYPE = "JWKS"
        const val TYPE_HTTP_SUFFIX = "/.well-known/jwks.json"
        const val DID = "did:web:"
    }
}