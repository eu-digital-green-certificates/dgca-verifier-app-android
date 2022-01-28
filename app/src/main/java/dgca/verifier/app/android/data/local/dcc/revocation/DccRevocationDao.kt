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
 *  Created by mykhailo.nester on 13/01/2022, 16:49
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import androidx.room.*
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationSliceLocal


@Dao
interface DccRevocationDao {

    @Query("SELECT * FROM dcc_revocation_kid_metadata WHERE kid is :kid")
    suspend fun getDccRevocationKidMetadataBy(kid: String): DccRevocationKidMetadataLocal?

    @Query("SELECT * FROM dcc_revocation_partition WHERE id is :partitionId AND kid is :kid")
    suspend fun getDccRevocationPartitionBy(partitionId: String, kid: String): DccRevocationPartitionLocal?

    @Query("SELECT * FROM dcc_revocation_partition WHERE kid is :kid AND x is :x AND y is :y")
    suspend fun getDccRevocationPartition(kid: String, x: Char?, y: Char?): DccRevocationPartitionLocal?

    @Query("SELECT * FROM dcc_revocation_slice WHERE kid is :kid AND x is :x AND y is :y AND cid is :cid")
    suspend fun getChunkSlices(kid: String, x: Char?, y: Char?, cid: String): List<DccRevocationSliceLocal>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DccRevocationKidMetadataLocal): Long

    @Update(entity = DccRevocationKidMetadataLocal::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: DccRevocationKidMetadataLocal): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DccRevocationPartitionLocal): Long

    @Update(entity = DccRevocationPartitionLocal::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: DccRevocationPartitionLocal): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: DccRevocationSliceLocal): Long

    @Update(entity = DccRevocationSliceLocal::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: DccRevocationSliceLocal): Int

    @Transaction
    fun upsert(entity: DccRevocationKidMetadataLocal) {
        val id = insert(entity)
        if (id == -1L) {
            update(entity)
        }
    }

    @Transaction
    fun upsert(entity: DccRevocationPartitionLocal) {
        val id = insert(entity)
        if (id == -1L) {
            update(entity)
        }
    }

    @Transaction
    fun upsert(entity: DccRevocationSliceLocal) {
        val id = insert(entity)
        if (id == -1L) {
            update(entity)
        }
    }

    @Transaction
    suspend fun removeOutdatedKidItems(kidList: List<String>) {
        removeOutdatedKidMetadata(kidList)
        removeOutdatedPartition(kidList)
        removeOutdatedSlices(kidList)
    }

    @Query("DELETE FROM dcc_revocation_kid_metadata WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedKidMetadata(kidList: List<String>)

    @Query("DELETE FROM dcc_revocation_partition WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedPartition(kidList: List<String>)

    @Query("DELETE FROM dcc_revocation_slice WHERE kid NOT IN (:kidList)")
    suspend fun removeOutdatedSlices(kidList: List<String>)

    @Query("DELETE FROM dcc_revocation_kid_metadata WHERE expires <= :currentTime")
    suspend fun deleteExpiredKIDs(currentTime: Long)

    @Query("DELETE FROM dcc_revocation_partition WHERE expires <= :currentTime")
    suspend fun deleteExpiredPartitions(currentTime: Long)

    @Query("DELETE FROM dcc_revocation_slice WHERE expires <= :currentTime")
    suspend fun deleteExpiredSlices(currentTime: Long)

    @Query("DELETE FROM dcc_revocation_slice WHERE kid is :kid AND cid NOT IN (:chunksIds)")
    suspend fun deleteOutdatedSlicesForPartitionId(kid: String, chunksIds: List<String>)

//    TODO: not used below

    @Query("SELECT * FROM dcc_revocation_kid_metadata WHERE kid is :kid")
    fun getDccRevocationKidMetadataListBy(kid: String): List<DccRevocationKidMetadataLocal>

    @Query("DELETE FROM dcc_revocation_kid_metadata WHERE kid is :kid")
    fun deleteDccRevocationKidMetadataListBy(kid: String)

    @Query("SELECT * FROM dcc_revocation_partition WHERE kid is :kid")
    fun getDccRevocationPartitionListBy(kid: String): List<DccRevocationPartitionLocal>

    @Query("DELETE FROM dcc_revocation_partition WHERE id is :partitionId")
    fun deleteDccRevocationPartitionBy(partitionId: String)

    @Insert(entity = DccRevocationPartitionLocal::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertList(entity: List<DccRevocationPartitionLocal>)

    @Insert(entity = DccRevocationSliceLocal::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertSlicesList(entity: List<DccRevocationSliceLocal>)
}