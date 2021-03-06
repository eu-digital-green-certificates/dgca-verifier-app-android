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

import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dcc.app.revocation.domain.model.RevocationKidData
import okhttp3.ResponseBody

interface RevocationRepository {

    @Throws(Exception::class)
    suspend fun getRevocationLists(baseUrl: String): List<RevocationKidData>?

    @Throws(Exception::class)
    suspend fun getRevocationPartitions(
        baseUrl: String,
        lastUpdated: String?,
        sliceType: SliceType,
        kid: String,
    ): List<RevocationPartitionResponse>?

    @Throws(Exception::class)
    suspend fun getPartitionChunks(
        baseUrl: String,
        sliceType: SliceType,
        kid: String,
        partitionId: String?,
        cidList: List<String>
    ): ResponseBody?

    @Throws(Exception::class)
    suspend fun getRevocationChunk(
        baseUrl: String,
        lastUpdated: String,
        sliceType: SliceType,
        kid: String,
        id: String?,
        chunkId: String
    ): ResponseBody?

    @Throws(Exception::class)
    suspend fun getRevocationChunkSlices(
        baseUrl: String,
        lastUpdated: String,
        sliceType: SliceType,
        kid: String,
        partitionId: String?,
        cid: String,
        sidList: List<String>
    ): ResponseBody?

    @Throws(Exception::class)
    suspend fun getSlice(
        baseUrl: String,
        lastUpdated: String,
        sliceType: SliceType,
        kid: String,
        partitionId: String?,
        cid: String,
        sid: String
    ): ResponseBody?

    suspend fun getMetadataByKid(kid: String): DccRevocationKidMetadata?

    suspend fun getLocalRevocationPartition(
        partitionId: String?,
        kid: String
    ): DccRevocationPartition?

    suspend fun getRevocationPartition(kid: String, x: Char?, y: Char?): DccRevocationPartition?

    suspend fun getChunkSlices(
        kid: String,
        x: Char?,
        y: Char?,
        cid: String,
        currentTime: Long
    ): List<DccRevocationSlice>

    suspend fun saveKidMetadata(dccRevocationKidMetadata: DccRevocationKidMetadata)

    suspend fun savePartition(partitionData: DccRevocationPartition)

    suspend fun saveSlice(dccRevocationSlice: DccRevocationSlice)

    suspend fun deleteOutdatedKidItems(notInKidList: List<String>)

    suspend fun deleteExpiredData(currentTime: Long)

    suspend fun deleteOutdatedSlicesForPartitionId(kid: String, chunksIds: List<String>)
}