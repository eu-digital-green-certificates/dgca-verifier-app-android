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
 *  Created by mykhailo.nester on 05/01/2022, 13:41
 */

package dcc.app.revocation.domain.usacase

import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import kotlinx.coroutines.CoroutineDispatcher
import timber.log.Timber
import javax.inject.Inject

class GetRevocationDataUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<Unit, Any>(dispatcher, errorHandler) {

    override suspend fun invoke(params: Any) {
        val resultList = repository.getRevocationLists()

//        TODO: Delete all KID entries in all tables which are not on this list.
//         Store KID metadata to DB.

        resultList.forEach { getPartition(it.kid) }
    }

    private suspend fun getPartition(kid: String) {
        Timber.d("Get partition for kid: $kid")
        val partitionData = repository.getRevocationPartition(kid)

//        TODO: update partitions in DB

        partitionData?.meta?.content?.chunks?.forEach {

//            TODO: check if exist in db before fetch
            getChunk(kid, partitionData.id, it.chunk.cid)
        }
    }

    private suspend fun getChunk(kid: String, id: String, cid: Int) {
        Timber.d("Get chunk for kid: $kid, partitionID: $id, cid: $cid")
        val chunk = repository.getRevocationChunk(kid, id, cid)

//        TODO: update chunk in DB
    }
}