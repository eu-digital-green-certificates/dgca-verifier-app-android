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

package dgca.verifier.app.android.vc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.vc.data.JwsTokenParser
import dgca.verifier.app.android.vc.data.JwtHeader
import dgca.verifier.app.android.vc.data.JwtPayload
import dgca.verifier.app.android.vc.data.remote.VcApiService
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VcViewModel @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val jwsTokenParser: JwsTokenParser,
    private val vcApiService: VcApiService
) : ViewModel() {

    fun validate(jwt: String) {
        viewModelScope.launch {
            val jwsObject = jwsTokenParser.parse(jwt) ?: return@launch

            val header: JwtHeader = objectMapper.readValue(jwsObject.header)
            Timber.d("header: $header")
            val jwtPayload: JwtPayload = objectMapper.readValue(jwsObject.payload)
            Timber.d("payload: $jwtPayload")

            val issuer = "" //encoded.iss
            resolveIssuer(issuer)
        }
    }

    private suspend fun resolveIssuer(issuer: String) {
        val result = vcApiService.resolveIssuer(issuer)
    }
}