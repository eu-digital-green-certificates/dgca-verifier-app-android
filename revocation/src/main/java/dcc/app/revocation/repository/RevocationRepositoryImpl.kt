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
 *  Created by mykhailo.nester on 24/12/2021, 15:29
 */

package dcc.app.revocation.repository

import dcc.app.revocation.data.RevocationPreferences
import dcc.app.revocation.data.containsServerError
import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dcc.app.revocation.data.network.RevocationService
import dcc.app.revocation.data.network.mapper.toRevocationKidData
import dcc.app.revocation.data.network.model.RevocationChunkResponse
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.data.network.model.RevocationSliceResponse
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dcc.app.revocation.domain.model.RevocationKidData
import retrofit2.HttpException
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class RevocationRepositoryImpl @Inject constructor(
    private val revocationService: RevocationService,
    private val revocationPreferences: RevocationPreferences,
    private val dccRevocationLocalDataSource: DccRevocationLocalDataSource
) : RevocationRepository {

    @Throws(Exception::class)
    override suspend fun getRevocationLists(): List<RevocationKidData> {
        val eTag = revocationPreferences.eTag ?: ""
        val response = revocationService.getRevocationLists(eTag)

        if (response.containsServerError()) {
            throw HttpException(response)
        }
        revocationPreferences.eTag = response.headers()["eTag"]

        return response.body()?.map { it.toRevocationKidData() } ?: emptyList()
    }

    @Throws(Exception::class)
    override suspend fun getRevocationPartitions(tag: String, kid: String): List<RevocationPartitionResponse>? {
        val eTag = revocationPreferences.eTag ?: ""
        val response = revocationService.getRevocationListPartitions(eTag, tag, kid)


        if (response.containsServerError()) {
            throw HttpException(response)
        }

        return response.body()
    }

    @Throws(Exception::class)
    override suspend fun getRevocationChunk(kid: String, id: String, chunkId: String): RevocationChunkResponse? {
        val eTag = revocationPreferences.eTag ?: ""
        val response = revocationService.getRevocationChunk(eTag, kid, id, chunkId)

        if (response.containsServerError()) {
            throw HttpException(response)
        }

        return response.body()
    }

    override suspend fun getSlice(kid: String, partitionId: String, cid: String, sid: String): RevocationSliceResponse? {
        val eTag = revocationPreferences.eTag ?: ""
        val response = revocationService.getRevocationChunkSlice(eTag, kid, partitionId, cid, sid)

        if (response.containsServerError()) {
            throw HttpException(response)
        }

        return response.body()
    }

    override suspend fun getMetadataByKid(kid: String): DccRevocationKidMetadata? =
        dccRevocationLocalDataSource.getDccRevocationKidMetadataBy(kid)

    override suspend fun getLocalRevocationPartition(partitionId: String, kid: String): DccRevocationPartition? =
        dccRevocationLocalDataSource.getPartitionById(partitionId, kid)

    override suspend fun saveKidMetadata(dccRevocationKidMetadata: DccRevocationKidMetadata) {
        dccRevocationLocalDataSource.addOrUpdate(dccRevocationKidMetadata)
    }

    override suspend fun savePartition(partitionData: DccRevocationPartition) {
        dccRevocationLocalDataSource.addOrUpdate(partitionData)
    }

    override suspend fun saveSlice(dccRevocationSlice: DccRevocationSlice) {
        dccRevocationLocalDataSource.addOrUpdate(dccRevocationSlice)
    }

    override suspend fun deleteOutdatedKidItems(notInKidList: List<String>) {
        dccRevocationLocalDataSource.removeOutdatedKidItems(notInKidList)
    }

    override suspend fun deleteOutdatedChunksForPartitionId(partitionId: String, partitionChunkIds: List<String>) {
        dccRevocationLocalDataSource.removeOutdatedPartitionChunks(partitionId, partitionChunkIds)
    }

    override suspend fun deleteExpiredData(currentTime: Long) {
        dccRevocationLocalDataSource.deleteExpiredKIDs(currentTime)
        dccRevocationLocalDataSource.deleteExpiredPartitions(currentTime)
        dccRevocationLocalDataSource.deleteExpireSlices(currentTime)
    }
}