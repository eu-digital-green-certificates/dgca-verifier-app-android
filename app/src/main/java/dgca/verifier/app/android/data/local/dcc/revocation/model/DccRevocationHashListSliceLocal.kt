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
 *  Created by osarapulov on 1/8/22, 3:04 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation.model

import androidx.room.*

@Entity(
    tableName = "dcc_revocation_hashlist_slice",
    indices = [
        Index(
            value = ["sid", "x", "y", "hash"],
            unique = true
        )
    ],
    foreignKeys = [ForeignKey(
        entity = DccRevocationSliceLocal::class,
        parentColumns = arrayOf("sid"),
        childColumns = arrayOf("sid"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class DccRevocationHashListSliceLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sid: String,
    val x: Char?,
    val y: Char?,
    @ColumnInfo(name = "hash", typeAffinity = ColumnInfo.BLOB)
    val hash: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DccRevocationHashListSliceLocal

        if (id != other.id) return false
        if (sid != other.sid) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (!hash.contentEquals(other.hash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + sid.hashCode()
        result = 31 * result + (x?.hashCode() ?: 0)
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + hash.contentHashCode()
        return result
    }
}