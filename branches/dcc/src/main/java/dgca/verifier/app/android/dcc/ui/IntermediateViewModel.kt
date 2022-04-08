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
 *  Created by osarapulov on 3/17/22, 3:02 PM
 */

package dgca.verifier.app.android.dcc.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.dcc.data.ConfigRepository
import dgca.verifier.app.android.dcc.data.local.Preferences
import dgca.verifier.app.android.dcc.settings.debug.DccSelectCountryData
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

sealed class IntermediateResult {
    object ProgressResult : IntermediateResult()
    object RetryResult : IntermediateResult()
    class CountrySelectedResult(val selectedCountryIsoCode: String) : IntermediateResult()
    class CountryNotSelectedResult(val selectCountryData: DccSelectCountryData) : IntermediateResult()
}

@HiltViewModel
class IntermediateViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val countriesRepository: CountriesRepository,
    private val preferences: Preferences
) : ViewModel() {

    private val _result = MutableLiveData<IntermediateResult>(IntermediateResult.ProgressResult)
    val result: LiveData<IntermediateResult> = _result


    private suspend fun fetchLocalCountries(): List<String> {
        return try {
            countriesRepository.getCountries().firstOrNull() ?: emptyList()
        } catch (exception: Exception) {
            Timber.e(exception, "Error loading available countries on settings screen")
            emptyList()
        }
    }

    private suspend fun fetchRemoteCountries(): List<String> {
        return try {
            val config = configRepository.local().getConfig()
            val versionName = "1.0.0" // TODO: update BuildConfig.VERSION_NAME
            countriesRepository.preLoadCountries(
                config.getCountriesUrl(versionName)
            )
            fetchLocalCountries()
        } catch (error: Throwable) {
            Timber.e(error, "error refreshing keys")
            emptyList()
        }
    }

    private suspend fun fetchCountries(): List<String> {
        val countries = fetchLocalCountries()
        return if (countries.isEmpty()) {
            fetchRemoteCountries()
        } else {
            countries
        }
    }

    fun saveCountrySelected(selectCountryData: DccSelectCountryData) {
        preferences.selectedCountryIsoCode = selectCountryData.selectedCountryIsoCode
        _result.value = IntermediateResult.CountrySelectedResult(selectCountryData.selectedCountryIsoCode!!)
    }

    init {
        viewModelScope.launch {
            var result: IntermediateResult
            withContext(Dispatchers.IO) {
                val selectedCountryIsoCode = preferences.selectedCountryIsoCode
                result = if (selectedCountryIsoCode?.isNotBlank() == true) {
                    IntermediateResult.CountrySelectedResult(selectedCountryIsoCode)
                } else {
                    val availableCountries = fetchCountries()
                    if (availableCountries.isEmpty()) {
                        IntermediateResult.RetryResult
                    } else {
                        val countriesSet = availableCountries.toSet()
                        IntermediateResult.CountryNotSelectedResult(
                            DccSelectCountryData(
                                countriesSet,
                                availableCountries.first()
                            )
                        )
                    }
                }
            }
            _result.value = result
        }
    }
}