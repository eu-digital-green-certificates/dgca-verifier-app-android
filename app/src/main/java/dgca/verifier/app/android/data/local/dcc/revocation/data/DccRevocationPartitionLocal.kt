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

package dgca.verifier.app.android.data.local.dcc.revocation.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dcc.app.revocation.domain.model.DccRevocationHashType
import java.time.ZonedDateTime

@Entity(
    tableName = "dcc_revocation_partition",
    foreignKeys = [
        ForeignKey(
            entity = DccRevocationKidMetadataLocal::class,
            parentColumns = arrayOf("kid", "hashType"),
            childColumns = arrayOf("kid", "hashType"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["kid", "x", "y", "pid"],
            unique = true
        ),
        Index(
            value = ["pid"],
            unique = true
        ),
        Index(
            value = ["pid", "version"],
            unique = true
        )
    ]
)
data class DccRevocationPartitionLocal(
    @PrimaryKey(autoGenerate = true)
    val partitionId: Long = 0,
    val kid: String,
    val x: Byte?,
    val y: Byte?,
    // Partition id
    val pid: String,
    val hashType: DccRevocationHashType,
    val version: String,
    val expiration: ZonedDateTime,
    val chunks: String
)
