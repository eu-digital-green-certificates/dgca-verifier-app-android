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
 *  Created by osarapulov on 3/17/22, 1:53 PM
 */

package dgca.verifier.app.android.dcc.data.local.countries

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dgca.verifier.app.android.dcc.data.local.model.CountryLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface CountriesDao {
    @Query("SELECT * from countries")
    fun getAll(): Flow<List<CountryLocal>>

    @Insert
    fun insertAll(vararg countriesLocal: CountryLocal)

    @Query("DELETE FROM countries")
    fun deleteAll()
}