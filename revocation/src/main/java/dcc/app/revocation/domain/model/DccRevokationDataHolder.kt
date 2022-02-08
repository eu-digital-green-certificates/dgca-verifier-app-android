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
 *  Created by mykhailo.nester on 20/01/2022, 18:48
 */

package dcc.app.revocation.domain.model

data class DccRevokationDataHolder(
    val kid: String,
    val uci: String,
    val coUvci: String,
    val cose: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DccRevokationDataHolder

        if (kid != other.kid) return false
        if (uci != other.uci) return false
        if (coUvci != other.coUvci) return false
        if (!cose.contentEquals(other.cose)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.hashCode()
        result = 31 * result + uci.hashCode()
        result = 31 * result + coUvci.hashCode()
        result = 31 * result + cose.contentHashCode()
        return result
    }
}