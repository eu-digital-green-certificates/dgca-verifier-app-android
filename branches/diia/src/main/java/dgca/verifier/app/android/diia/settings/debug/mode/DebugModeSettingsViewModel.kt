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
 *  Created by osarapulov on 3/17/22, 3:03 PM
 */

package dgca.verifier.app.android.diia.settings.debug.mode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.diia.data.local.Preferences
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DebugModeSettingsViewModel @Inject constructor(
    private val preferences: Preferences,
    private val countriesRepository: CountriesRepository
) : ViewModel() {

    private val _countriesData: MutableLiveData<CountriesData> = MutableLiveData()
    val countriesData: LiveData<CountriesData> = _countriesData

    private val _debugModeState: MutableLiveData<DebugModeState> =
        MutableLiveData(DebugModeState.OFF)
    val debugModeState: LiveData<DebugModeState> = _debugModeState

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF
            }.apply {
                _debugModeState.value = this
            }

            val countriesData: CountriesData = withContext(Dispatchers.IO) {
                val selectedCountriesCodes: Set<String> =
                    preferences.debugModeSelectedCountriesCodes ?: emptySet()
                val availableCountries: Set<String> = try {
                    mutableSetOf<String>().apply {
                        countriesRepository.getCountries().firstOrNull()?.let {
                            this.addAll(it)
                        }
                    } + selectedCountriesCodes
                } catch (exception: Exception) {
                    selectedCountriesCodes
                }
                return@withContext CountriesData(availableCountries, selectedCountriesCodes)
            }

            _countriesData.value = countriesData
        }
    }

    fun saveSelectedDebugMode(debugModeState: DebugModeState) {
        if (debugModeState != _debugModeState.value) {
            _debugModeState.value = debugModeState
            preferences.debugModeState = debugModeState.toString()
        }
    }

    fun saveSelectedCountries(countriesData: CountriesData) {
        _countriesData.value = countriesData
        preferences.debugModeSelectedCountriesCodes = countriesData.selectedCountriesCodes
    }
}
