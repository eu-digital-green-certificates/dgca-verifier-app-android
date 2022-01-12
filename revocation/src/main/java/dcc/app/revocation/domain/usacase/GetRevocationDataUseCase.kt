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
import dcc.app.revocation.BuildConfig
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.domain.model.RevocationSettingsData
import dcc.app.revocation.network.mapper.toDccRevocationHashType
import dcc.app.revocation.parseDate
import kotlinx.coroutines.CoroutineDispatcher
import timber.log.Timber
import javax.inject.Inject

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
//     TODO: clarify:   Match list
        val metadataLocal = repository.getMetadataByKid(kid)

        //  Initial sync. no KID metadata in DB
        if (metadataLocal == null) {
            revocationKidData.settings.forEach {
                saveKidMetadata(kid, it)
                getPartition(kid)
            }
            return
        }

        revocationKidData.settings.forEach {

            // If mode has changed remove all data related to this kid
            if (it.mode != metadataLocal.mode) {
                repository.removeOutdatedKidItems(listOf(kid))
            }

            // TODO: update DB records
//            saveKidMetadata(kid, it)


            // Check the last modified date for each kid. If last date per kid < then received date,
            // call for the kid /{kid}/partitions to receive the metadata objects. If last date > then received date, do nothing
            val lastModified = repository.getLastModifiedForKid(kid)
            if (lastModified.parseDate()?.isBefore(it.lastUpdated.parseDate()) == true) {
                getPartition(kid)
            }
        }
    }

    private suspend fun saveKidMetadata(kid: String, revocationSettingsData: RevocationSettingsData) {
        repository.saveKidMetadata(
            DccRevocationKidMetadata(
                kid,
                revocationSettingsData.hashType,
                revocationSettingsData.mode,
                revocationSettingsData.tag
            )
        )
    }

    private suspend fun getPartition(kid: String) {
        Timber.d("Get partition for kid: $kid")
        repository.getRevocationPartition(kid)?.let { partition ->
            val partitionChunkIds = mutableListOf<Int>()

//            Store the received metadata objects to the Revocation Partition Table. For this purpose filter by the received tag.
//            If tag == appversion, store the chunks object as binary content. Override existing entries. If tag!=appversion, ignore the object
            partition.meta.forEach { meta ->
                if (meta.tag == BuildConfig.REVOCATION_APP_VERSION) {
                    repository.savePartition(
                        DccRevocationPartition(
                            kid = partition.kid,
                            x = partition.x.toByte(),
                            y = partition.y.toByte(),
                            pid = partition.id,
                            hashType = meta.content.hashType.toDccRevocationHashType(),
                            version = partition.version,
                            expiration = partition.expires.parseDate()?.toZonedDateTime()!!,
                            chunks = Gson().toJson(meta.content.chunks) // TODO: or BINARY
                        )
                    )

//                  TODO: delete expired partitions/chunks

                    partitionChunkIds.addAll(meta.content.chunks.map { it.chunk.cid })
                }
            }

            // Remove all Chunks which are not more available (delete from .. not in .. ).
            repository.removeOutdatedChunksForPartitionId(partition.id, partitionChunkIds)

            // Download from /{kid}/partitions/{id}/chunks/{chunkId} the missing chunks
            partitionChunkIds.forEach {
                getChunk(kid, partition.id, it)
            }
        }
    }

    private suspend fun getChunk(kid: String, id: String, cid: Int) {
        Timber.d("Get chunk for kid: $kid, partitionID: $id, cid: $cid")
        val chunk = repository.getRevocationChunk(kid, id, cid)

//        TODO: update chunk in DB
    }
}