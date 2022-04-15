/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 2:52 PM
 */

package dgca.verifier.app.android.dcc.settings

import androidx.lifecycle.*
import com.android.app.dcc.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dcc.app.revocation.domain.usacase.GetRevocationDataUseCase
import dgca.verifier.app.android.dcc.data.ConfigRepository
import dgca.verifier.app.android.dcc.data.VerifierRepository
import dgca.verifier.app.android.dcc.data.local.Preferences
import dgca.verifier.app.android.dcc.settings.debug.DccSelectCountryData
import dgca.verifier.app.android.dcc.settings.debug.mode.DebugModeState
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DccSettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val verifierRepository: VerifierRepository,
    private val countriesRepository: CountriesRepository,
    private val preferences: Preferences,
    private val getRevocationDataUseCase: GetRevocationDataUseCase
) : ViewModel(), LifecycleObserver {

    private val _inProgress = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean> = _inProgress

    val lastSyncLiveData: LiveData<Long> = verifierRepository.getLastPubKeysSyncTimeMillis()
    val lastRevocationSyncTime = MutableLiveData(verifierRepository.getLastRevocationSyncTimeMillis())

    private val _lastCountriesSyncLiveData = MutableLiveData(preferences.lastCountriesSyncTimeMillis)
    val lastCountriesSyncLiveData: LiveData<Long> = _lastCountriesSyncLiveData

    private val _debugModeState: MutableLiveData<DebugModeState> = MutableLiveData(DebugModeState.OFF)
    val debugModeState: LiveData<DebugModeState> = _debugModeState

    private val _selectCountryData = MutableLiveData(DccSelectCountryData())
    val selectCountryData: LiveData<DccSelectCountryData> = _selectCountryData

    init {
        viewModelScope.launch {
            var lastCountriesSync: Long
            var selectedCountryIsoCode: String?
            val availableCountries = mutableSetOf<String>()
            withContext(Dispatchers.IO) {
                try {
                    countriesRepository.getCountries().firstOrNull()?.let {
                        availableCountries.addAll(it)
                    }
                } catch (exception: Exception) {
                    Timber.e(exception, "Error loading available countries on settings screen")
                    preferences.lastCountriesSyncTimeMillis = 0
                    _lastCountriesSyncLiveData.postValue(0)
                }
                lastCountriesSync = countriesRepository.getLastCountriesSync()
                selectedCountryIsoCode = countriesRepository.getSelectedCountryIsoCode()
            }

            _lastCountriesSyncLiveData.value = lastCountriesSync
            _selectCountryData.value = DccSelectCountryData(availableCountries, selectedCountryIsoCode)
        }
    }

    internal fun reset() {
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

    fun syncCountries() {
        viewModelScope.launch {
            _inProgress.value = true
            withContext(Dispatchers.IO) {
                try {
                    val config = configRepository.local().getConfig()
                    val versionName = "1.0.0" // TODO: update BuildConfig.VERSION_NAME
                    countriesRepository.preLoadCountries(
                        config.getCountriesUrl(versionName)
                    )
                    _lastCountriesSyncLiveData.postValue(countriesRepository.getLastCountriesSync())
                } catch (error: Throwable) {
                    Timber.e(error, "error refreshing keys")
                    preferences.lastCountriesSyncTimeMillis = 0
                    _lastCountriesSyncLiveData.postValue(0)
                }
            }
            _inProgress.value = false
        }
    }

    fun saveCountrySelected(selectCountryData: DccSelectCountryData) {
        _selectCountryData.value = selectCountryData
        preferences.selectedCountryIsoCode = selectCountryData.selectedCountryIsoCode
    }

    fun syncRevocation() {
        _inProgress.value = true
        getRevocationDataUseCase.execute(
            viewModelScope,
            onFailure = { Timber.d("error refreshing revocation: $it") },
            onSuccess = { lastRevocationSyncTime.value = verifierRepository.getLastRevocationSyncTimeMillis() },
            onComplete = { _inProgress.value = false }
        )
    }
}