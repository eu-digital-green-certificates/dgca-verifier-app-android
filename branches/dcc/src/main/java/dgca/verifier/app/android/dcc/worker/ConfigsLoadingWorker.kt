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
 *  Created by osarapulov on 3/17/22, 2:25 PM
 */

package dgca.verifier.app.android.dcc.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dgca.verifier.app.android.dcc.data.ConfigRepository
import dgca.verifier.app.android.dcc.data.local.Preferences
import timber.log.Timber

@HiltWorker
class ConfigsLoadingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val configRepository: ConfigRepository,
    private val preferences: Preferences
) : Worker(context, workParams) {

    override fun doWork(): Result {
        try {
            val config = configRepository.getConfig()
            Timber.d("Config: $config")
        } catch (error: Throwable) {
            Timber.d(error, "Config Loading Error: $error")
            preferences.lastCountriesSyncTimeMillis = 0
            return Result.retry()
        }
        return Result.success()
    }
}
