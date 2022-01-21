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
    val x: Char?,
    val y: Char?,
    val z: Char?,
    val expires: String,
    val chunks: Map<String, Map<String, Slice>>
)

data class Slice(
    val type: SliceType,
    val version: String,
    val hash: String
)

enum class SliceType {
    Hash, Bloom
}