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
 *  Created by mykhailo.nester on 24/12/2021, 15:51
 */

package dcc.app.revocation.domain

import dcc.app.revocation.data.network.model.RevocationChunkResponse
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.data.network.model.RevocationSliceResponse
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dcc.app.revocation.domain.model.RevocationKidData

interface RevocationRepository {

    @Throws(Exception::class)
    suspend fun getRevocationLists(): List<RevocationKidData>

    @Throws(Exception::class)
    suspend fun getRevocationPartitions(tag: String, kid: String): List<RevocationPartitionResponse>?

    @Throws(Exception::class)
    suspend fun getRevocationChunk(kid: String, id: String, chunkId: String): RevocationChunkResponse?

    @Throws(Exception::class)
    suspend fun getSlice(kid: String, partitionId: String, cid: String, sid: String): RevocationSliceResponse?

    suspend fun getMetadataByKid(kid: String): DccRevocationKidMetadata?

    suspend fun getLocalRevocationPartition(partitionId: String, kid: String): DccRevocationPartition?

    suspend fun getRevocationPartition(kid: String, x: Char?, y: Char?): DccRevocationPartition?

    suspend fun getChunkSlices(sid: String, kid: String, x: Char?, y: Char?, cid: Char): DccRevocationSlice?

    suspend fun saveKidMetadata(dccRevocationKidMetadata: DccRevocationKidMetadata)

    suspend fun savePartition(partitionData: DccRevocationPartition)

    suspend fun saveSlice(dccRevocationSlice: DccRevocationSlice)

    suspend fun deleteOutdatedKidItems(notInKidList: List<String>)

    suspend fun deleteOutdatedChunksForPartitionId(partitionId: String, partitionChunkIds: List<String>)

    suspend fun deleteExpiredData(currentTime: Long)
}