/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
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
 *  Created by mykhailo.nester on 29/12/2021, 18:20
 */

package dcc.app.revocation.mock

import com.upokecenter.cbor.CBORObject

fun getPartition(): ByteArray {
    val chunkObject = CBORObject.NewMap()
        .Add("type", "HASHLIST".toByteArray())
        .Add("version", "1.0".toByteArray())
        .Add("cid", "119".toByteArray())

    val hashListCBORObject = CBORObject.NewMap()
        .Add("kid", "33wdd=".toByteArray())
        .Add("x", 1.toByte())
        .Add("y", 4.toByte())
        .Add("chunkId", "119".toByteArray())
        .Add(
            "hash", CBORObject.NewArray()
                .Add("eda41aab68a44e6e71b912abc8f2fd7844a599346a9303886c5abe96f15375f4".toByteArray())
                .Add("edd41aab68a44e6e71b912abc8f2fd7844a599346a9303886c5abe96f15375f4".toByteArray())
                .Add("edc41aab68a44e6e71b912abc8f2fd7844a599346a9303886c5abe96f15375f4".toByteArray())
        )
        .Add("issued", 1640792972592)
        .Add("expired", 1640792972592)

    val hashListObject = CBORObject.NewMap()
        .Add("section", "a".toByteArray())
        .Add("hashes", hashListCBORObject)
        .Add("chunk", chunkObject)

    val contentObject = CBORObject.NewMap()
        .Add("hashType", "SIGNATURE".toByteArray())
        .Add("hashes", hashListObject)

    val contentMap = CBORObject.NewMap()
        .Add("tag", "testTag".toByteArray())
        .Add("content", contentObject)

    val result = CBORObject.NewMap().apply {
        Add("kid", CBORObject.FromObject("33wdd=").EncodeToBytes())
        Add("x", 1.toByte())
        Add("y", 4.toByte())
        Add("version", "1.0".toByteArray())
        Add("meta", CBORObject.NewArray().Add(contentMap))
    }

    return result.EncodeToBytes()
}