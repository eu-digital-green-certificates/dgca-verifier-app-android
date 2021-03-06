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
 *  Created by osarapulov on 3/17/22, 1:52 PM
 */

package dgca.verifier.app.android.dcc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dcc.app.revocation.data.local.EnumConverter
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.DccRevocationDao
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationSliceLocal
import dgca.verifier.app.android.dcc.data.local.model.Key
import dgca.verifier.app.android.dcc.data.local.rules.Converters

@Database(
    entities = [
        Key::class,
        DccRevocationKidMetadataLocal::class,
        DccRevocationPartitionLocal::class,
        DccRevocationSliceLocal::class
    ],
    version = 1
)
@TypeConverters(Converters::class, EnumConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun keyDao(): KeyDao

    abstract fun dccRevocationPartitionDao(): DccRevocationDao
}
