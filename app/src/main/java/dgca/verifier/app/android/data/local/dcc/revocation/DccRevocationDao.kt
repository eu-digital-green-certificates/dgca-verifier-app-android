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
 *  Created by osarapulov on 12/27/21, 10:13 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import androidx.room.*
import dgca.verifier.app.android.data.local.dcc.revocation.data.DccRevocationChunkLocal
import dgca.verifier.app.android.data.local.dcc.revocation.data.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.data.local.dcc.revocation.data.DccRevocationPartitionLocal

@Dao
interface DccRevocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dccRevocationKidMetadataLocal: DccRevocationKidMetadataLocal)

    @Query("SELECT * FROM dcc_revocation_kid_metadata WHERE kid LIKE :kid")
    fun getDccRevocationKidMetadataListBy(kid: String): List<DccRevocationKidMetadataLocal>

    @Query("DELETE FROM dcc_revocation_kid_metadata WHERE kid = :kid")
    fun deleteDccRevocationKidMetadataListBy(kid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dccRevocationPartitionLocal: DccRevocationPartitionLocal)

    @Query("SELECT * FROM dcc_revocation_partition WHERE kid LIKE :kid")
    fun getDccRevocationPartitionListBy(
        kid: String
    ): List<DccRevocationPartitionLocal>

    @Query("DELETE FROM dcc_revocation_partition WHERE pid = :partitionId")
    fun deleteDccRevocationPartitionBy(partitionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dccRevocationChunkLocal: DccRevocationChunkLocal)

    @Query("SELECT * FROM dcc_revocation_chunk WHERE kid LIKE :kid")
    fun getDccRevocationChunkList(
        kid: String
    ): List<DccRevocationChunkLocal>

    @Query("DELETE FROM dcc_revocation_chunk WHERE cid = :chunkId")
    fun deleteDccRevocationChunkBy(chunkId: String)

    @Query("DELETE FROM dcc_revocation_kid_metadata WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedKidMetadata(kidList: List<String>)

    @Query("DELETE FROM dcc_revocation_partition WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedPartition(kidList: List<String>)

    @Query("DELETE FROM dcc_revocation_chunk WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedChunks(kidList: List<String>)

    @Transaction
    suspend fun removeOutdatedKidItems(kidList: List<String>) {
        removeOutdatedKidMetadata(kidList)
        removeOutdatedPartition(kidList)
        removeOutdatedChunks(kidList)
    }

    @Query("SELECT * FROM dcc_revocation_kid_metadata WHERE kid LIKE :kid")
    suspend fun getDccRevocationKidMetadataBy(kid: String): DccRevocationKidMetadataLocal?

    @Query("DELETE FROM dcc_revocation_chunk WHERE pid = :partitionId AND cid NOT IN (:partitionChunkIds)")
    suspend fun removeOutdatedPartitionChunks(partitionId: String, partitionChunkIds: List<Int>)
}