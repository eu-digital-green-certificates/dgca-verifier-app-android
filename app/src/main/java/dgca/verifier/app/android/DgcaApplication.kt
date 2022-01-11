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
 *  Created by Mykhailo Nester on 4/23/21 9:48 AM
 */

package dgca.verifier.app.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import dcc.app.revocation.worker.RevocationWorker
import dgca.verifier.app.android.data.ConfigRepository
import dgca.verifier.app.android.worker.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DgcaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var configDataSource: ConfigRepository

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        GlobalScope.launch {
            try {
                configDataSource.local().getConfig()
            } catch (fileNotFoundException: FileNotFoundException) {
                throw IllegalStateException("It's required to provide config json files. As an example may be used 'app/src/acc/assets/verifier-context.jsonc' or 'app/src/tst/assets/verifier-context.jsonc' files")
            }
        }

//        TODO: for test purpose uncomment when finish.
        WorkManager.getInstance(this).apply {
//            schedulePeriodicWorker<ConfigsLoadingWorker>(WORKER_CONFIGS)
//            schedulePeriodicWorker<RulesLoadWorker>(WORKER_RULES)
//            schedulePeriodicWorker<LoadKeysWorker>(WORKER_KEYS)
//            schedulePeriodicWorker<CountriesLoadWorker>(WORKER_COUNTRIES)
//            schedulePeriodicWorker<ValueSetsLoadWorker>(WORKER_VALUESETS)
//            schedulePeriodicWorker<RevocationWorker>(WORKER_REVOCATION)
            scheduleOneTimeWorker<RevocationWorker>(WORKER_REVOCATION)
        }

        Timber.i("DGCA version ${BuildConfig.VERSION_NAME} is starting")
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

    private inline fun <reified T : ListenableWorker> WorkManager.scheduleOneTimeWorker(workerId: String) =
        this.enqueueUniqueWork(
            workerId, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<T>()
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
        const val WORKER_CONFIGS = "workerConfigs"
        const val WORKER_REVOCATION = "workerRevocation"
        const val WORKER_RULES = "workerRules"
        const val WORKER_KEYS = "workerKeys"
        const val WORKER_COUNTRIES = "workerCountries"
        const val WORKER_VALUESETS = "workerValueSets"
    }
}