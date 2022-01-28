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
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.data.network.model.Slice
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.isEqualTo
import kotlinx.coroutines.CoroutineDispatcher
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.time.ZonedDateTime
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
        repository.deleteOutdatedKidItems(newKidItems.map { it.kid })

        newKidItems.forEach { revocationKidData ->
            checkKidMetadata(revocationKidData)
        }

        // Delete expired data
        repository.deleteExpiredData(System.currentTimeMillis())
    }

    private suspend fun checkKidMetadata(revocationKidData: RevocationKidData) {
        val kid = revocationKidData.kid
        val metadataLocal = repository.getMetadataByKid(kid)

        //  Initial sync. no KID metadata in DB
        if (metadataLocal == null) {
            saveKidMetadata(kid, revocationKidData)
            getPartitions(kid)
            return
        }

        // If mode has changed remove all data related to this kid
        if (revocationKidData.mode != metadataLocal.mode) {
            repository.deleteOutdatedKidItems(listOf(kid))
        }

        // Insert/Update new KID metadata
        saveKidMetadata(kid, revocationKidData)

        // Check the last modified date for each kid. If the last date per kid != received date,
        // call for the kid /{kid}/partitions to receive the metadata objects. If last date ==  received date, do nothing.
        if (metadataLocal.lastUpdated != revocationKidData.lastUpdated) {
            getPartitions(kid)
        }
    }

    private suspend fun saveKidMetadata(kid: String, revocationSettingsData: RevocationKidData) {
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

    private suspend fun getPartitions(kid: String) {
        repository.getRevocationPartitions(SUPPORTED_TAG, kid)?.forEach { partition ->
            handlePartition(kid, partition)
        }
    }

    private suspend fun handlePartition(kid: String, remotePartition: RevocationPartitionResponse) {
        val localPartition = repository.getLocalRevocationPartition(remotePartition.id, kid)

        if (localPartition != null) {
            // Optimization - section/chunk validation what changed
            compareChunksWithLocal(kid, localPartition, remotePartition)
        } else {
            // Initial sync. load all chunks

            // TODO: Use slices for now. Clarify response for chunks
//            remotePartition.chunks.keys.forEach {
//                 Download from /{kid}/partitions/{id}/chunks/{chunkId} the missing chunks
//                getChunk(kid, remotePartition.id, it)
//            }

            remotePartition.chunks.forEach { (remoteChunkKey, remoteChunkValue) ->
                getSlices(kid, remotePartition, remoteChunkKey, remoteChunkValue)
            }
        }

        savePartition(kid, remotePartition)

        val chunksIds = mutableListOf<String>()
        remotePartition.chunks.keys.forEach { chunksIds.add(it) }
        // Remove all Chunks which are not more available (delete from .. not in .. ).
        repository.deleteOutdatedSlicesForPartitionId(kid, chunksIds)
    }

    private suspend fun compareChunksWithLocal(
        kid: String,
        localPartition: DccRevocationPartition,
        remotePartition: RevocationPartitionResponse
    ) {
        val type: Type = object : TypeToken<Map<String, Map<String, Slice>>>() {}.type
        val localChunks = Gson().fromJson<Map<String, Map<String, Slice>>>(localPartition.chunks, type)

        remotePartition.chunks.forEach { (remoteChunkKey, remoteChunkValue) ->
            val localSlices = localChunks[remoteChunkKey]
            if (localSlices == null) {
                // When chunk not found load from api

                // TODO: Use slices for now. Clarify response for chunks
//                getChunk(kid, remotePartition.id, remoteChunkKey)

                remotePartition.chunks.forEach { (remoteChunkKey, remoteChunkValue) ->
                    getSlices(kid, remotePartition, remoteChunkKey, remoteChunkValue)
                }

            } else {
                val slices = mutableMapOf<String, Slice>()

                // Compare slices with local chunk slices
                remoteChunkValue.forEach { (remoteSliceKey, remoteSliceValue) ->
                    val localSlice = localSlices[remoteSliceKey]
                    if (localSlice == null || !localSlice.isEqualTo(remoteSliceValue)) {
                        slices[remoteSliceKey] = remoteSliceValue
                    }
                }

                // If less than 50% slices has changed load slices by CIDs otherwise load whole chunk
                if (slices.size < remoteChunkValue.size / 2) {
                    getSlices(kid, remotePartition, remoteChunkKey, slices)
                } else {
                    // TODO: Use slices for now. Clarify response for chunks
//                    getChunk(kid, remotePartition.id, remoteChunkKey)
                    getSlices(kid, remotePartition, remoteChunkKey, slices)
                }
            }
        }
    }

    private suspend fun savePartition(kid: String, partition: RevocationPartitionResponse) {
        repository.savePartition(
            DccRevocationPartition(
                id = partition.id,
                kid = kid,
                x = partition.x,
                y = partition.y,
                expires = partition.expires,
                chunks = Gson().toJson(partition.chunks)
            )
        )
    }

    private suspend fun getChunk(kid: String, id: String, cid: String) {
        val chunk = repository.getRevocationChunk(kid, id, cid)

//        TODO: update chunk in DB
    }

    private suspend fun getSlices(
        kid: String,
        partition: RevocationPartitionResponse,
        cid: String,
        slices: Map<String, Slice>
    ) {
        slices.forEach { (key, value) ->
            val sid = value.hash
            val response = repository.getSlice(kid, partition.id, cid, sid)

//            TODO: parse Gzip and replace content

            ByteArrayOutputStream().use {
//                bloomFilter.writeTo(it)
//                val content = it.toByteArray()

                repository.saveSlice(
                    DccRevocationSlice(
                        sid = sid,
                        kid = kid,
                        x = partition.x,
                        y = partition.y,
                        cid = cid,
                        type = value.type,
                        version = value.version,
                        expires = ZonedDateTime.parse(key),
                        content = "".toByteArray()
                    )
                )
            }
        }
    }
}