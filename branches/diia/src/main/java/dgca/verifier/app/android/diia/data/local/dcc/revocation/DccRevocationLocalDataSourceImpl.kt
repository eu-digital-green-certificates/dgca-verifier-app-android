/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 2:23 PM
 */

package dgca.verifier.app.android.diia.data.local.diia.revocation

import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice
import dgca.verifier.app.android.diia.data.local.diia.revocation.mapper.fromLocal
import dgca.verifier.app.android.diia.data.local.diia.revocation.mapper.toLocal

class DccRevocationLocalDataSourceImpl(private val dccRevocationDao: DccRevocationDao) :
    DccRevocationLocalDataSource {

    override suspend fun getDccRevocationKidMetadataBy(kid: String): DccRevocationKidMetadata? =
        dccRevocationDao.getDccRevocationKidMetadataBy(kid)?.fromLocal()

    override suspend fun getPartitionById(
        partitionId: String?,
        kid: String
    ): DccRevocationPartition? =
        dccRevocationDao.getDccRevocationPartitionBy(partitionId, kid)?.fromLocal()

    override suspend fun getRevocationPartition(
        kid: String,
        x: Char?,
        y: Char?
    ): DccRevocationPartition? =
        dccRevocationDao.getDccRevocationPartition(kid, x, y)?.fromLocal()

    override suspend fun getChunkSlices(
        kid: String,
        x: Char?,
        y: Char?,
        cid: String,
        currentTime: Long
    ): List<DccRevocationSlice> =
        dccRevocationDao.getChunkSlices(kid, x, y, cid, currentTime).map { it.fromLocal() }

    override fun addOrUpdate(dccRevocationKidMetadata: DccRevocationKidMetadata) {
        dccRevocationDao.upsert(dccRevocationKidMetadata.toLocal())
    }

    override fun addOrUpdate(dccRevocationPartition: DccRevocationPartition) {
        dccRevocationDao.upsert(dccRevocationPartition.toLocal())
    }

    override suspend fun addOrUpdate(dccRevocationSlice: DccRevocationSlice) {
        dccRevocationDao.upsert(dccRevocationSlice.toLocal())
    }

    override suspend fun removeOutdatedKidItems(kidList: List<String>) {
        dccRevocationDao.removeOutdatedKidItems(kidList)
    }

    override suspend fun deleteExpiredKIDs(currentTime: Long) {
        dccRevocationDao.deleteExpiredKIDs(currentTime)
    }

    override suspend fun deleteExpiredPartitions(currentTime: Long) {
        dccRevocationDao.deleteExpiredPartitions(currentTime)
    }

    override suspend fun deleteExpireSlices(currentTime: Long) {
        dccRevocationDao.deleteExpiredSlices(currentTime)
    }

    override suspend fun deleteOutdatedSlicesForPartitionId(kid: String, chunksIds: List<String>) {
        dccRevocationDao.deleteOutdatedSlicesForPartitionId(kid, chunksIds)
    }
}
