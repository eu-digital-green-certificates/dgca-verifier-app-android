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
 *  Created by osarapulov on 1/8/22, 10:52 AM
 */

package dgca.verifier.app.android.data.local.dcc.revocation.data

import dcc.app.revocation.data.DccRevocationChunk
import dcc.app.revocation.data.DccRevocationKidMetadata
import dcc.app.revocation.data.DccRevocationPartition

fun DccRevocationKidMetadata.toLocal(): DccRevocationKidMetadataLocal {
    return DccRevocationKidMetadataLocal(
        kid = kid,
        hashType = hashType,
        mode = mode,
        tag = tag
    )
}

fun DccRevocationKidMetadataLocal.fromLocal(): DccRevocationKidMetadata {
    return DccRevocationKidMetadata(
        kid = kid,
        hashType = hashType,
        mode = mode,
        tag = tag
    )
}

fun DccRevocationPartitionLocal.fromLocal(): DccRevocationPartition {
    return DccRevocationPartition(
        kid = kid,
        x = x,
        y = y,
        pid = pid,
        hashType = hashType,
        version = version,
        expiration = expiration,
        chunks = chunks
    )
}

fun DccRevocationPartition.toLocal(): DccRevocationPartitionLocal {
    return DccRevocationPartitionLocal(
        kid = kid,
        x = x,
        y = y,
        pid = pid,
        hashType = hashType,
        version = version,
        expiration = expiration,
        chunks = chunks
    )
}

fun DccRevocationChunkLocal.fromLocal(): DccRevocationChunk {
    return DccRevocationChunk(
        kid = kid,
        x = x,
        y = y,
        pid = pid,
        cid = cid,
        type = type,
        version = version,
        expiration = expiration,
        section = section,
        content = content
    )
}

fun DccRevocationChunk.toLocal(): DccRevocationChunkLocal {
    return DccRevocationChunkLocal(
        kid = kid,
        x = x,
        y = y,
        pid = pid,
        cid = cid,
        type = type,
        version = version,
        expiration = expiration,
        section = section,
        content = content
    )
}