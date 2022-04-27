/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 25/03/2022, 23:02
 */

package dgca.verifier.app.android.vc.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.vc.data.local.VcPreferences
import dgca.verifier.app.android.vc.domain.GetTrustListUseCase
import dgca.verifier.app.android.vc.ui.Event
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getTrustListUseCase: GetTrustListUseCase,
    private val preferences: VcPreferences
) : ViewModel() {

    private val _lastTimeSync = MutableLiveData<Long>()
    val lastTimeSync: LiveData<Long> = _lastTimeSync

    private val _event = MutableLiveData<Event<ViewEvent>>()
    val event: LiveData<Event<ViewEvent>> = _event

    init {
        _lastTimeSync.value = preferences.trustListSyncTimeMillis
    }

    fun reloadTrustList() {
        _event.value = Event(ViewEvent.OnLoading(true))
        getTrustListUseCase.execute(viewModelScope,
            onFailure = { _event.value = Event(ViewEvent.OnError(it.toString())) },
            onComplete = {
                _lastTimeSync.value = preferences.trustListSyncTimeMillis
                _event.value = Event(ViewEvent.OnLoading(false))
            }
        )
    }

    sealed class ViewEvent {
        data class OnLoading(val isLoading: Boolean) : ViewEvent()
        data class OnError(val error: String) : ViewEvent()
    }
}