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
import dgca.verifier.app.android.vc.data.remote.model.SignerCertificate
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
            val certificates = vcRepository.loadTrustList()
            certificates.forEach {
                when (it.type) {
                    IssuerType.HTTP -> resolveIssuer(it)
                    IssuerType.DID -> resolveDid(it)
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

    //    TODO: resolve urls check if http ends with .well-knonw.json
    private suspend fun resolveIssuer(certificate: SignerCertificate) {
        val result = vcRepository.resolveIssuer(certificate.url)
        if (result.isNotEmpty()) {
            vcRepository.saveIssuer(certificate, result)
        }
    }

    //    TODO: resolve did:web part if not resolved
    private suspend fun resolveDid(certificate: SignerCertificate) {
        val result = vcRepository.resolveIssuerByDid(certificate.url)
        if (result.isNotEmpty()) {
            vcRepository.saveDidIssuer(certificate, result)
        }
    }
}