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
 *  Created by osarapulov on 1/3/22, 4:35 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import dcc.app.revocation.data.source.DccRevocationPartition

fun DccRevocationPartitionLocal.fromLocal(): DccRevocationPartition {
    return DccRevocationPartition(
        kid = kid,
        firstDccHashByte = firstDccHashByte,
        secondDccHashByte = secondDccHashByte,
        revocationDataBlob = revokedDccsBlob
    )
}

fun DccRevocationPartition.toLocal(): DccRevocationPartitionLocal {
    return DccRevocationPartitionLocal(
        kid = kid,
        firstDccHashByte = firstDccHashByte,
        secondDccHashByte = secondDccHashByte,
        revokedDccsBlob = revocationDataBlob
    )
}
