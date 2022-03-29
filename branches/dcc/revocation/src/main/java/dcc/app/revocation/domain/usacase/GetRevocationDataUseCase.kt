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
import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.domain.toBase64Url
import dcc.app.revocation.isEqualTo
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.ResponseBody
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.InputStream
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPInputStream
import javax.inject.Inject


class GetRevocationDataUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<Unit, Any>(dispatcher, errorHandler) {

    private val sliceType = SliceType.BLOOMFILTER

    override suspend fun invoke(params: Any) {
        // Load list of KIDs
        val newKidItems = repository.getRevocationLists() ?: return

        // Remove all entities not matching KIDs from list
        repository.deleteOutdatedKidItems(newKidItems.map { it.kid })

        newKidItems.forEach { revocationKidData ->
            checkKidMetadata(revocationKidData)
        }

        // Delete expired data
        repository.deleteExpiredData(ChronoUnit.MICROS.between(Instant.EPOCH, ZonedDateTime.now().toInstant()))
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
                kid = kid,
                hashType = revocationSettingsData.hashType,
                mode = revocationSettingsData.mode,
                expires = revocationSettingsData.expires,
                lastUpdated = revocationSettingsData.lastUpdated
            )
        )
    }

    private suspend fun getPartitions(kid: String) {
        val kidUrlSafe = kid.toBase64Url()
        repository.getRevocationPartitions(sliceType, kidUrlSafe)?.forEach { partition ->
            handlePartition(kid, partition)
        }
    }

    private suspend fun handlePartition(kid: String, remotePartition: RevocationPartitionResponse) {
        val localPartition = repository.getLocalRevocationPartition(remotePartition.id, kid)
        if (localPartition != null) {
            // Optimization - section/chunk validation what changed
            compareChunksWithLocal(kid, localPartition, remotePartition)
        } else {
            // Initial sync. load all chunks for partition
            val result =
                repository.getPartitionChunks(
                    sliceType = sliceType,
                    kid = kid.toBase64Url(),
                    partitionId = remotePartition.id,
                    cidList = remotePartition.chunks.map { it.key })
            handlePartitionSlices(kid, remotePartition, result)
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
        val localChunks =
            Gson().fromJson<Map<String, Map<String, Slice>>>(localPartition.chunks, type)

        remotePartition.chunks.forEach { (remoteChunkKey, remoteChunkValue) ->
            val localSlices = localChunks[remoteChunkKey]
            if (localSlices == null) {
                // When chunk not found load from api
                val response = repository.getRevocationChunk(
                    sliceType = sliceType,
                    kid = kid.toBase64Url(),
                    id = remotePartition.id,
                    chunkId = remoteChunkKey
                )
                handlePartitionSlices(kid, remotePartition, response)

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
                    // Load updated or missing slices by sid list.
                    val response = repository.getRevocationChunkSlices(
                        sliceType = sliceType,
                        kid = kid.toBase64Url(),
                        partitionId = remotePartition.id,
                        cid = remoteChunkKey,
                        sidList = slices.mapValues { it.value }.map { it.value.hash }
                    )
                    handlePartitionSlices(kid, remotePartition, response)
                } else {
                    val response = repository.getRevocationChunk(
                        sliceType = sliceType,
                        kid = kid.toBase64Url(),
                        id = remotePartition.id,
                        chunkId = remoteChunkKey
                    )
                    handlePartitionSlices(kid, remotePartition, response)
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

    private suspend fun handlePartitionSlices(
        kid: String,
        partition: RevocationPartitionResponse,
        response: ResponseBody?
    ) {
        response ?: return

        readTarStream(response.byteStream(),
            onNextEntry = { stream, entry ->
                val bytes = stream.readBytes()
                val segments = entry.name.split("/")
                val cid = segments[segments.size - CID_POSITION]
                val sid = segments.last()

                val chunkValue = partition.chunks[cid] ?: return

                val sliceMap = chunkValue.filterValues { it.hash == sid }
                val sliceExpires = sliceMap.keys.first()
                val sliceValue = sliceMap.values.first()

                repository.saveSlice(
                    DccRevocationSlice(
                        sid = sid,
                        kid = kid,
                        x = partition.x,
                        y = partition.y,
                        cid = cid,
                        type = sliceValue.type,
                        version = sliceValue.version,
                        expires = ZonedDateTime.parse(sliceExpires),
                        content = bytes
                    )
                )
            }
        )
    }

    private inline fun readTarStream(byteStream: InputStream, onNextEntry: (InputStream, TarArchiveEntry) -> Unit) {
        val tarInputStream = TarArchiveInputStream(GZIPInputStream(byteStream))
        var entry = tarInputStream.nextTarEntry
        tarInputStream.use { stream ->
            while (entry != null) {
                onNextEntry(stream, entry)
                entry = tarInputStream.nextTarEntry
            }
        }
    }

    companion object {
        private const val CID_POSITION = 2
    }
}