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
 *  Created by osarapulov on 12/27/21, 9:59 PM
 */

package dcc.app.revocation.data.local

import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationPartition
import dcc.app.revocation.domain.model.DccRevocationSlice

interface DccRevocationLocalDataSource {

    suspend fun getDccRevocationKidMetadataBy(kid: String): DccRevocationKidMetadata?

    suspend fun getPartitionById(partitionId: String?, kid: String): DccRevocationPartition?

    suspend fun getRevocationPartition(kid: String, x: Char?, y: Char?): DccRevocationPartition?

    suspend fun getChunkSlices(
        kid: String,
        x: Char?,
        y: Char?,
        cid: String
    ): List<DccRevocationSlice>

    fun addOrUpdate(dccRevocationKidMetadata: DccRevocationKidMetadata)

    fun addOrUpdate(dccRevocationPartition: DccRevocationPartition)

    suspend fun addOrUpdate(dccRevocationSlice: DccRevocationSlice)

    suspend fun removeOutdatedKidItems(kidList: List<String>)

    suspend fun deleteExpiredKIDs(currentTime: Long)

    suspend fun deleteExpiredPartitions(currentTime: Long)

    suspend fun deleteExpireSlices(currentTime: Long)

    suspend fun deleteOutdatedSlicesForPartitionId(kid: String, chunksIds: List<String>)
}