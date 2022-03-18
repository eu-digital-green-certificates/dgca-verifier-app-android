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
 *  Created by osarapulov on 3/17/22, 2:41 PM
 */

package dgca.verifier.app.android.dcc.data.local.dcc.revocation.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "dcc_revocation_partition",
    indices = [
        Index(
            value = ["kid", "x", "y", "id"],
            unique = true
        ),
        Index(
            value = ["id"],
            unique = true
        )
    ]
)
data class DccRevocationPartitionLocal(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val id: String?,
    val kid: String,
    val x: Char?,
    val y: Char?,
    val expires: ZonedDateTime,
    val chunks: String
)
