/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by mykhailo.nester on 24/12/2021, 15:50
 */

package dcc.app.revocation.domain.usacase

import com.upokecenter.cbor.CBORObject
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetRevocationListPartitionsUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<List<String>, String>(dispatcher, errorHandler) {

    override suspend fun invoke(params: String): List<String> {
        val result = repository.getRevocationListPartitions(params)

        if (result.isNotEmpty()) {
//            TODO: convert to data model
            val cbor = CBORObject.DecodeFromBytes(result)
            val kid = CBORObject.DecodeFromBytes(cbor.get("kid").GetByteString()).AsString()

            return listOf(kid)

        }

        return emptyList()
    }
}