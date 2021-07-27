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
 *  Created by osarapulov on 7/26/21 1:51 PM
 */

package dgca.verifier.app.android.data.remote.valuesets

import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetIdentifierRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import retrofit2.Response

class DefaultValueSetsRemoteDataSource(private val apiService: ValueSetsApiService) :
    ValueSetsRemoteDataSource {
    override suspend fun getValueSetsIdentifiers(url: String): List<ValueSetIdentifierRemote> {
        val response: Response<List<ValueSetIdentifierRemote>> =
            apiService.getValueSetsIdentifiers(url)
        return response.body() ?: listOf()
    }

    override suspend fun getValueSet(url: String): ValueSetRemote? {
        val ruleResponse: Response<ValueSetRemote> = apiService.getValueSet(url)
        return ruleResponse.body()
    }
}