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
 *  Created by osarapulov on 9/3/21 9:41 PM
 */

package dgca.verifier.app.android.settings.debug.mode

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
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

    init {
        viewModelScope.launch {
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
        preferences.debugModeState = debugModeState.toString()
    }

    fun saveSelectedCountries(countriesData: CountriesData) {
        _countriesData.value = countriesData
        preferences.debugModeSelectedCountriesCodes = countriesData.selectedCountriesCodes
    }

    val debugModeState: LiveData<DebugModeState> = liveData {
        emit(preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF)
    }
}