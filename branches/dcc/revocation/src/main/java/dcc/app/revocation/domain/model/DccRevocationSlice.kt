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
 *  Created by osarapulov on 1/8/22, 11:57 AM
 */

package dcc.app.revocation.domain.model

import dcc.app.revocation.data.network.model.SliceType
import java.time.ZonedDateTime

data class DccRevocationSlice(
    val sid: String,
    val kid: String,
    val x: Char?,
    val y: Char?,
    val cid: String,
    val type: SliceType,
    val version: String,
    val expires: ZonedDateTime,
    val content: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DccRevocationSlice

        if (sid != other.sid) return false
        if (kid != other.kid) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (cid != other.cid) return false
        if (type != other.type) return false
        if (version != other.version) return false
        if (expires != other.expires) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sid.hashCode()
        result = 31 * result + kid.hashCode()
        result = 31 * result + (x?.hashCode() ?: 0)
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + cid.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + expires.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}