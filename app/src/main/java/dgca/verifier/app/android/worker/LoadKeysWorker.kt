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
 *  Created by osarapulov on 4/30/21 5:01 PM
 */

package dgca.verifier.app.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dgca.verifier.app.android.BuildConfig
import dgca.verifier.app.android.data.ConfigRepository
import dgca.verifier.app.android.data.VerifierRepository
import timber.log.Timber

@HiltWorker
class LoadKeysWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val configRepository: ConfigRepository,
    private val verifierRepository: VerifierRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        Timber.d("key fetching start")
        val config = configRepository.local().getConfig()
        val versionName = BuildConfig.VERSION_NAME
        val res = verifierRepository.fetchCertificates(
            config.getStatusUrl(versionName),
            config.getUpdateUrl(versionName)
        )
        Timber.d("key fetching result: ${res == true}")
        return if (res == true) Result.success() else Result.retry()
    }
}