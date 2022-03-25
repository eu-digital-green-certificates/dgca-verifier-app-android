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
 *  Created by mykhailo.nester on 23/03/2022, 22:35
 */

package dgca.verifier.app.android.vc

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.*
import com.android.app.base.Processor
import com.android.app.base.RESULT_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import dgca.verifier.app.android.vc.worker.TrustListLoadingWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VcProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) : Processor {

    override fun prefetchData() {
        Timber.d("Prefetching data...")
        WorkManager.getInstance(context).apply {
            schedulePeriodicWorker<TrustListLoadingWorker>(VC_WORKER_CONFIGS)
        }
    }

    override fun isApplicable(input: String): Intent? {
//        if applicable
        val isVc = true

        if (!isVc) {
            return null
        }

        return Intent("com.android.app.vc.View", Uri.parse("verifier://vc")).apply {
            putExtra(RESULT_KEY, input)
        }
    }

    override fun getSettingsIntent(): Pair<String, Intent> {
        return Pair("Vc", Intent("com.android.app.vc.View", Uri.parse("settings://vc")))
    }

    private inline fun <reified T : ListenableWorker> WorkManager.schedulePeriodicWorker(workerId: String) =
        this.enqueueUniquePeriodicWork(
            workerId, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<T>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MAX_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        )

    companion object {
        const val VC_WORKER_CONFIGS = "vcWorkerConfigs"
    }
}
