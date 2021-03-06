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
 *  Created by mykhailo.nester on 05/01/2022, 13:30
 */

package dcc.app.revocation.data.network.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class RevocationPartitionResponse(

    @JsonProperty("id")
    val id: String?,

    @JsonProperty("x")
    val x: Char?,

    @JsonProperty("y")
    val y: Char?,

    @JsonProperty("lastUpdated")
    val lastUpdated: ZonedDateTime,

    @JsonProperty("expired")
    val expires: ZonedDateTime,

    @JsonProperty("chunks")
    val chunks: Map<String, Map<String, Slice>>
)

data class Slice(

    @JsonProperty("type")
    val type: SliceType,

    @JsonProperty("version")
    val version: String,

    @JsonProperty("hash")
    val hash: String
)

enum class SliceType {
    BLOOMFILTER, VARHASHLIST
}