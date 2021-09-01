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
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import javax.inject.Inject

@HiltViewModel
class CodeReaderViewModel @Inject constructor(
    private val countriesRepository: CountriesRepository,
    private val preferences: Preferences
) : ViewModel() {
    private val _countries: MediatorLiveData<Pair<List<String>, String?>> = MediatorLiveData()
    val countries: LiveData<Pair<List<String>, String?>> = _countries
    private val _selectedCountry: LiveData<String?> = liveData {
        emit(preferences.selectedCountryIsoCode)
    }

    fun isDebugModeEnabled(): Boolean? = preferences.isDebugModeEnabled

    fun selectCountry(countryIsoCode: String) {
        preferences.selectedCountryIsoCode = countryIsoCode
    }

    init {
        _countries.addSource(countriesRepository.getCountries().asLiveData()) {
            _countries.value = Pair(it, _countries.value?.second)
        }

        _countries.addSource(_selectedCountry) {
            if (_countries.value?.second == null || _countries.value?.second != it) {
                _countries.value = Pair(_countries.value?.first ?: emptyList(), it ?: "")
            }
        }
    }
}