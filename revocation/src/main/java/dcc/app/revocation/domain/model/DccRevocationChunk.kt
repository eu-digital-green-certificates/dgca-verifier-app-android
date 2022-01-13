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
 *  Created by osarapulov on 1/8/22, 2:03 PM
 */

package dcc.app.revocation.domain.model

import java.time.ZonedDateTime

data class DccRevocationChunk (
    val kid: String,
    val x: Byte?,
    val y: Byte?,
    // Partition id
    val pid: String,
    // Chunk id
    val cid: String,
    val type: DccChunkType,
    val version: String,
    val expiration: ZonedDateTime,
    val section: String,
    val content: String
) {
    fun contains(hash: String): Boolean {
        if (content.isBlank()) return false

        var leftIndex = 0
        var rightIndex = content.length - SHA_256_STRING_LENGTH
        while (leftIndex <= rightIndex) {
            val middleIndex = (leftIndex + rightIndex) / 2
            val middleHash = content.substring(middleIndex, middleIndex + SHA_256_STRING_LENGTH)
            val diff = middleHash.compareTo(hash)
            if (diff < 0) {
                leftIndex = middleIndex + SHA_256_STRING_LENGTH
            } else if (diff > 0) {
                rightIndex = middleIndex - SHA_256_STRING_LENGTH
            } else {
                return true
            }
        }
        return false
    }

    companion object {
        private const val SHA_256_STRING_LENGTH = 64
    }
}
