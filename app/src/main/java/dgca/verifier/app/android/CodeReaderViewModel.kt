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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CodeReaderViewModel @Inject constructor(private val countriesRepository: CountriesRepository) :
    ViewModel() {
    private val _countries = MutableLiveData<List<String>>(emptyList())
    val countries: LiveData<List<String>> = _countries

    init {
        viewModelScope.launch {
            val countries: List<String>
            withContext(Dispatchers.IO) {
                countries = countriesRepository.getCountries()
            }
            _countries.value =
                countries.map { COUNTRIES_MAP[it] ?: it }.sortedBy { Locale("", it).displayCountry }
        }
    }

    companion object {
        private val COUNTRIES_MAP = mapOf("el" to "gr")
    }
}