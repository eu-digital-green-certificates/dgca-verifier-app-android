/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 4/29/21 11:51 PM
 */

package it.ministerodellasalute.verificaC19.data.local

import androidx.room.*

@Dao
interface KeyDao {
    @Query("SELECT * FROM keys")
    fun getAll(): List<Key>

    @Query("SELECT * FROM keys WHERE kid IN (:keyIds)")
    fun getAllByIds(keyIds: Array<String>): List<Key>

    @Query("SELECT * FROM keys WHERE kid LIKE :kid LIMIT 1")
    fun getById(kid: String): Key?

    @Query("DELETE FROM keys WHERE kid = :kid")
    fun deleteById(kid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(key: Key)

    @Delete
    fun delete(key: Key)

    @Query("DELETE FROM keys WHERE kid NOT IN (:keyIds)")
    fun deleteAllExcept(keyIds: Array<String>)
}