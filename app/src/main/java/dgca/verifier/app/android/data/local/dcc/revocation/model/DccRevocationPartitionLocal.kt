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
 *  Created by osarapulov on 1/8/22, 12:03 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dcc_revocation_partition",
    foreignKeys = [
        ForeignKey(
            entity = DccRevocationKidMetadataLocal::class,
            parentColumns = arrayOf("kid"),
            childColumns = arrayOf("kid"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["kid", "x", "y", "z", "id"],
            unique = true
        ),
        Index(
            value = ["id"],
            unique = true
        )
    ]
)
data class DccRevocationPartitionLocal(
    @PrimaryKey
    val id: String,
    val kid: String,
    val x: Byte?,
    val y: Byte?,
    val z: Byte?,
    val expires: Long,
    val chunks: String
)
