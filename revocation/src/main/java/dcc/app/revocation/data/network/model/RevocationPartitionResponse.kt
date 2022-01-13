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

data class RevocationPartitionResponse(
    val id: String,
    val kid: String,
    val x: String,
    val y: String,
    val version: String,
    val expires: String,
    val meta: List<ContentMeta>
)

data class ContentMeta(
    val tag: String,
    val content: Content
)

data class Content(
    val hashType: HashType,
    val chunks: List<Chunks>
)

enum class HashType {
    SIGNATURE, UCI, COUNTRYCODEUCI
}

data class Chunks(
    val section: String,
    val chunk: Chunk
)

data class Chunk(
    val type: String,
    val cid: Int,
    val version: String
)

enum class ChunkType {
    HASH, BLOOM
}