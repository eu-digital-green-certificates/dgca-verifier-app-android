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

package dgca.verifier.app.android.data.local.dcc.revocation.mapper

import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.model.*
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationHashListSliceLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationSliceLocal

fun DccRevocationKidMetadata.toLocal(): DccRevocationKidMetadataLocal {
    return DccRevocationKidMetadataLocal(
        kid = kid,
        hashType = hashType,
        mode = mode,
        expires = expires,
        lastUpdated = lastUpdated
    )
}

fun DccRevocationKidMetadataLocal.fromLocal(): DccRevocationKidMetadata {
    return DccRevocationKidMetadata(
        kid = kid,
        hashType = hashType,
        mode = mode,
        expires = expires,
        lastUpdated = lastUpdated
    )
}

fun DccRevocationPartitionLocal.fromLocal(): DccRevocationPartition {
    return DccRevocationPartition(
        id = id,
        kid = kid,
        x = x,
        y = y,
        expires = expires,
        chunks = chunks
    )
}

fun DccRevocationPartition.toLocal(): DccRevocationPartitionLocal {
    return DccRevocationPartitionLocal(
        id = id,
        kid = kid,
        x = x,
        y = y,
        expires = expires,
        chunks = chunks
    )
}

fun DccRevocationSliceLocal.fromLocal(): DccRevocationSlice {
    return DccRevocationSlice(
        sid = sid,
        kid = kid,
        x = x,
        y = y,
        cid = cid,
        type = type.toSliceType(),
        version = version,
        expires = expires,
        content = content
    )
}

fun DccSliceType.toSliceType(): SliceType =
    when (this) {
        DccSliceType.HASH -> SliceType.Hash
        DccSliceType.BF -> SliceType.Bloom
    }

fun DccRevocationSlice.toLocal(): DccRevocationSliceLocal {
    return DccRevocationSliceLocal(
        sid = sid,
        kid = kid,
        x = x,
        y = y,
        cid = cid,
        type = type.toDccSliceType(),
        version = version,
        expires = expires,
        content = content
    )
}

fun SliceType.toDccSliceType(): DccSliceType =
    when (this) {
        SliceType.Hash -> DccSliceType.HASH
        SliceType.Bloom -> DccSliceType.BF
    }

fun DccRevocationHashListSlice.toLocal(): DccRevocationHashListSliceLocal =
    DccRevocationHashListSliceLocal(
        sid = sid,
        x = x,
        y = y,
        hash = hash
    )

fun DccRevocationHashListSliceLocal.fromLocal(): DccRevocationHashListSlice =
    DccRevocationHashListSlice(
        sid = sid,
        x = x,
        y = y,
        hash = hash
    )