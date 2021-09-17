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

package dgca.verifier.app.android

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.android.settings.debug.mode.DebugModeState
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CodeReaderViewModel @Inject constructor(
    countriesRepository: CountriesRepository,
    private val preferences: Preferences
) : ViewModel() {

    private val _countries = MediatorLiveData<List<String>>()
    val countries: LiveData<List<String>> = _countries

    private val _selectedCountry = MutableLiveData<String>()
    val selectedCountry: LiveData<String> = _selectedCountry

    val debugModeState: LiveData<DebugModeState> = liveData {
        emit(preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF)
    }

    fun selectCountry(countryIsoCode: String) {
        preferences.selectedCountryIsoCode = countryIsoCode
        _selectedCountry.value = countryIsoCode
    }

    init {
        viewModelScope.launch {
            countriesRepository.getCountries().collectLatest {
                _countries.value = it
                _selectedCountry.value = preferences.selectedCountryIsoCode
            }
        }
    }
}