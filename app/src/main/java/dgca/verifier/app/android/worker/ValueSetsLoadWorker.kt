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
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import timber.log.Timber

@HiltWorker
class ValueSetsLoadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val configRepository: ConfigRepository,
    private val valueSetsRepository: ValueSetsRepository
) : CoroutineWorker(context, workParams) {

    override suspend fun doWork(): Result {
        Timber.d("value sets loading start")
        return try {
            val config = configRepository.local().getConfig()
            val versionName = BuildConfig.VERSION_NAME
            valueSetsRepository.preLoad(
                config.getValueSetsUrl(versionName)
            )
            Timber.d("value sets loading succeeded")
            Result.success()
        } catch (error: Throwable) {
            Timber.d("value sets loading retry")
            Result.retry()
        }
    }
}