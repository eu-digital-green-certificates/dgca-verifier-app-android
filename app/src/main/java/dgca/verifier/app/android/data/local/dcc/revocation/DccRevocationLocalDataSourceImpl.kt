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
 *  Created by osarapulov on 12/27/21, 10:22 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import dcc.app.revocation.data.source.local.DccRevocationLocalDataSource
import dcc.app.revocation.domain.model.DccRevocationChunk
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dgca.verifier.app.android.data.local.dcc.revocation.data.fromLocal
import dgca.verifier.app.android.data.local.dcc.revocation.data.toLocal

class DccRevocationLocalDataSourceImpl(private val dccRevocationDao: DccRevocationDao) :
    DccRevocationLocalDataSource {
    override fun addOrUpdate(dccRevocationKidMetadata: DccRevocationKidMetadata) {
        dccRevocationDao.insert(dccRevocationKidMetadata.toLocal())
    }

    // TODO: List not possible
    override fun getDccRevocationKidMetadataListBy(kid: String) {
        dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
    }

    override fun removeDccRevocationKidMetadataBy(kid: String) {
        dccRevocationDao.deleteDccRevocationKidMetadataListBy(kid = kid)
    }

    override fun addOrUpdate(dccRevocationPartition: DccRevocationPartition) {
        dccRevocationDao.insert(dccRevocationPartition.toLocal())
    }

    override fun getDccRevocationPartitionListBy(kid: String): List<DccRevocationPartition> {
        return dccRevocationDao.getDccRevocationPartitionListBy(kid = kid)
            .map {
                it.fromLocal()
            }
    }

    override fun removeDccRevocationPartitionBy(pid: String) {
        dccRevocationDao.deleteDccRevocationPartitionBy(partitionId = pid)
    }

    override fun addOrUpdate(dccRevocationChunk: DccRevocationChunk) {
        dccRevocationDao.insert(dccRevocationChunk.toLocal())
    }

    override fun getDccRevocationChunkListBy(kid: String): List<DccRevocationChunk> {
        return dccRevocationDao.getDccRevocationChunkList(kid = kid)
            .map {
                it.fromLocal()
            }
    }

    override fun removeDccRevocationChunkListBy(cid: String) {
        dccRevocationDao.deleteDccRevocationChunkBy(chunkId = cid)
    }

    override suspend fun removeOutdatedKidItems(kidList: List<String>) {
        dccRevocationDao.removeOutdatedKidItems(kidList)
    }

    override suspend fun getDccRevocationKidMetadataBy(kid: String): DccRevocationKidMetadata? =
        dccRevocationDao.getDccRevocationKidMetadataBy(kid)?.fromLocal()

    override suspend fun removeOutdatedPartitionChunks(partitionId: String, partitionChunkIds: List<Int>) {
        dccRevocationDao.removeOutdatedPartitionChunks(partitionId, partitionChunkIds)
    }
}
