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

package dgca.verifier.app.android.reader

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dcc.app.revocation.domain.request.ChunkRequest
import dcc.app.revocation.domain.request.ListChunksRequest
import dcc.app.revocation.domain.usacase.GetRevocationChunkUseCase
import dcc.app.revocation.domain.usacase.GetRevocationListChunksUseCase
import dcc.app.revocation.domain.usacase.GetRevocationListPartitionsUseCase
import dcc.app.revocation.domain.usacase.GetRevocationListsUseCase
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.android.settings.debug.mode.DebugModeState
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CodeReaderViewModel @Inject constructor(
    countriesRepository: CountriesRepository,
    private val preferences: Preferences,

//    TODO: test purpose only
    private val getRevocationListsUseCase: GetRevocationListsUseCase,
    private val getRevocationListPartitionsUseCase: GetRevocationListPartitionsUseCase,
    private val getRevocationListChunksUseCase: GetRevocationListChunksUseCase,
    private val getRevocationChunkUseCase: GetRevocationChunkUseCase,
) : ViewModel() {

    private val _countries = MediatorLiveData<List<String>>()
    val countries: LiveData<List<String>> = _countries

    private val _selectedCountry = MutableLiveData<String>()
    val selectedCountry: LiveData<String> = _selectedCountry

    val debugModeState: LiveData<DebugModeState> = liveData {
        emit(preferences.debugModeState?.let { DebugModeState.valueOf(it) } ?: DebugModeState.OFF)
    }

    init {
        viewModelScope.launch {
            countriesRepository.getCountries().collectLatest {
                _countries.value = it
                _selectedCountry.value = preferences.selectedCountryIsoCode
            }
        }

        getRevocationData()
    }

    fun selectCountry(countryIsoCode: String) {
        preferences.selectedCountryIsoCode = countryIsoCode
        _selectedCountry.value = countryIsoCode
    }

    private fun getRevocationData() {
        getRevocationListsUseCase.execute(viewModelScope,
            onSuccess = {
                Timber.d("List loaded $it")
                getListPartitions(it.first())
            },
            onFailure = { Timber.d("List loading failed $it") },
            onComplete = { Timber.d("List loading completed") }
        )
    }

    private fun getListPartitions(kid: String) {
        getRevocationListPartitionsUseCase.execute(viewModelScope, kid,
            onSuccess = {
                Timber.d("Partition loaded $it")
                getListChunks("kid", "id")
            },
            onFailure = { Timber.d("Partition loading failed $it") },
            onComplete = { Timber.d("Partition loading completed") }
        )
    }

    private fun getListChunks(kid: String, id: String) {
        getRevocationListChunksUseCase.execute(viewModelScope, ListChunksRequest(kid, id),
            onSuccess = {
                Timber.d("List chunks loaded $it")
                getChunk("kid", "id", "chunkId")
            },
            onFailure = { Timber.d("List chunks loading failed $it") },
            onComplete = { Timber.d("List chunks loading completed") }
        )
    }

    private fun getChunk(kid: String, id: String, chunkId: String) {
        getRevocationChunkUseCase.execute(viewModelScope, ChunkRequest(kid, id, chunkId),
            onSuccess = {
                Timber.d("Chunk loaded $it")
            },
            onFailure = { Timber.d("Chunk loading failed $it") },
            onComplete = { Timber.d("Chunk loading completed") }
        )
    }
}