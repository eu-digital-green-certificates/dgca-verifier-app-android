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
 *  Created by mykhailo.nester on 4/24/21 2:54 PM
 */

package dgca.verifier.app.android.settings

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.BuildConfig
import dgca.verifier.app.android.data.ConfigRepository
import dgca.verifier.app.android.data.VerifierRepository
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.android.settings.debug.mode.DebugModeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val verifierRepository: VerifierRepository,
    private val preferences: Preferences
) : ViewModel(), LifecycleObserver {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    val lastSyncLiveData: LiveData<Long> = verifierRepository.getLastSyncTimeMillis()

    private val _debugModeState: MutableLiveData<DebugModeState> = MutableLiveData(DebugModeState.OFF)
    val debugModeState: LiveData<DebugModeState> = _debugModeState

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onLifeCycleStart() {
        viewModelScope.launch {
            val debugModeState: DebugModeState = withContext(Dispatchers.IO) {
                preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF
            }
            _debugModeState.value = debugModeState
        }
    }

    fun syncPublicKeys() {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                try {
                    val config = configRepository.local().getConfig()
                    val versionName = BuildConfig.VERSION_NAME
                    verifierRepository.fetchCertificates(
                        config.getStatusUrl(versionName),
                        config.getUpdateUrl(versionName)
                    )
                } catch (error: Throwable) {
                    Timber.e(error, "error refreshing keys")
                }
            }
            _inProgress.value = false
        }
    }
}