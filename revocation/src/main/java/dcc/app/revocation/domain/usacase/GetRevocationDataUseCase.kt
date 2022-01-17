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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dcc.app.revocation.data.network.model.Chunk
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.domain.model.RevocationSettingsData
import dcc.app.revocation.isEqualTo
import dcc.app.revocation.parseDate
import kotlinx.coroutines.CoroutineDispatcher
import java.lang.reflect.Type
import javax.inject.Inject

private const val SUPPORTED_TAG = "1.0"

class GetRevocationDataUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<Unit, Any>(dispatcher, errorHandler) {

    override suspend fun invoke(params: Any) {
        // Load list of KIDs
        val newKidItems = repository.getRevocationLists()

        // Remove all entities not matching KIDs from list
        repository.removeOutdatedKidItems(newKidItems.map { it.kid })

        newKidItems.forEach { revocationKidData ->
            checkKidMetadata(revocationKidData)
        }
    }

    private suspend fun checkKidMetadata(revocationKidData: RevocationKidData) {
        val kid = revocationKidData.kid
        val metadataLocal = repository.getMetadataByKid(kid)
        val settings = revocationKidData.settings

        //  Initial sync. no KID metadata in DB
        if (metadataLocal == null) {
            saveKidMetadata(kid, settings)
            getPartition(kid)
            return
        }

        // If mode has changed remove all data related to this kid
        if (settings.mode != metadataLocal.mode) {
            repository.removeOutdatedKidItems(listOf(kid))
        }

        // Insert/Update new KID metadata
        saveKidMetadata(kid, settings)

        // Check the last modified date for each kid. If the last date per kid != received date,
        // call for the kid /{kid}/partitions to receive the metadata objects. If last date ==  received date, do nothing.
        if (metadataLocal.lastUpdated != settings.lastUpdated) {
            getPartition(kid)
        }
    }

    private suspend fun saveKidMetadata(kid: String, revocationSettingsData: RevocationSettingsData) {
        repository.saveKidMetadata(
            DccRevocationKidMetadata(
                kid,
                revocationSettingsData.hashType,
                revocationSettingsData.mode,
                revocationSettingsData.expires,
                revocationSettingsData.lastUpdated
            )
        )
    }

    private suspend fun getPartition(kid: String) {
        repository.getRevocationPartition(SUPPORTED_TAG, kid)?.let { partitions ->
            partitions.forEach { partition ->
                handlePartition(kid, partition)
            }
        }
    }

    private suspend fun handlePartition(kid: String, remotePartition: RevocationPartitionResponse) {
        val localPartition = repository.getLocalRevocationPartition(remotePartition.id, kid)

        if (localPartition != null) {
            // Optimization - section/chunk validation what changed
            compareChunksWithLocal(kid, localPartition, remotePartition)
        } else {
            // Initial sync. load all chunks
            remotePartition.chunks.keys.forEach {
                // Download from /{kid}/partitions/{id}/chunks/{chunkId} the missing chunks
                getChunk(kid, remotePartition.id, it)
            }
        }

        savePartition(kid, remotePartition)

        val chunksIds = mutableListOf<String>()
        remotePartition.chunks.keys.forEach { chunksIds.add(it) }
        // Remove all Chunks which are not more available (delete from .. not in .. ).
        repository.removeOutdatedChunksForPartitionId(remotePartition.id, chunksIds)
    }

    private suspend fun compareChunksWithLocal(
        kid: String,
        localPartition: DccRevocationPartition,
        remotePartition: RevocationPartitionResponse
    ) {
        val type: Type = object : TypeToken<Map<String, Map<String, Chunk>>>() {}.type
        val localChunks = Gson().fromJson<Map<String, Map<String, Chunk>>>(localPartition.chunks, type)

        remotePartition.chunks.forEach { (remoteChunkKey, remoteChunkValue) ->
            val localSlices = localChunks[remoteChunkKey]
            if (localSlices == null) {
                // When chunk not found load from api
                getChunk(kid, remotePartition.id, remoteChunkKey)

            } else {
                val slicesIds = mutableListOf<String>()

                // Compare slices with local chunk slices
                remoteChunkValue.forEach { (remoteSliceKey, remoteSliceValue) ->
                    val localSlice = localSlices[remoteSliceKey]
                    if (localSlice == null || !localSlice.isEqualTo(remoteSliceValue)) {
                        slicesIds.add(remoteSliceValue.hash)
                    }
                }

                // If less than 50% slices has changed load slices by CIDs otherwise load whole chunk
                if (slicesIds.size < remoteChunkValue.size / 2) {
                    getSlices(kid, remotePartition.id, remoteChunkKey, slicesIds)
                } else {
                    getChunk(kid, remotePartition.id, remoteChunkKey)
                }
            }
        }
    }

    private suspend fun savePartition(kid: String, partition: RevocationPartitionResponse) {
        repository.savePartition(
            DccRevocationPartition(
                id = partition.id,
                kid = kid,
                x = partition.x?.toByte(),
                y = partition.y?.toByte(),
                z = partition.z?.toByte(),
                expires = partition.expires.parseDate()?.toZonedDateTime()!!,
                chunks = Gson().toJson(partition.chunks)
            )
        )
    }

    private suspend fun getChunk(kid: String, id: String, cid: String) {
        val chunk = repository.getRevocationChunk(kid, id, cid)


//        TODO: update chunk in DB
    }

    private suspend fun getSlices(kid: String, partitionId: String, cid: String, slicesIds: MutableList<String>) {
        slicesIds.forEach { sid ->
            val slice = repository.getSlice(kid, partitionId, cid, sid)

//        TODO: update slice in DB
        }
    }
}