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
 *  Created by mykhailo.nester on 11/01/2022, 20:55
 */

package dcc.app.revocation.data.network.mapper

import dcc.app.revocation.data.network.model.RevocationKIDResponse
import dcc.app.revocation.data.network.model.RevocationSettings
import dcc.app.revocation.domain.model.DccRevocationHashType
import dcc.app.revocation.domain.model.DccRevocationMode
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.domain.model.RevocationSettingsData

fun RevocationKIDResponse.toRevocationKidData(): RevocationKidData =
    RevocationKidData(
        kid = kid,
        settings = settings.toSettingsData()
    )

fun RevocationSettings.toSettingsData(): RevocationSettingsData =
    RevocationSettingsData(
        mode = mode.toDccRevocationMode(),
        hashType = hashType.toDccRevocationHashType(),
        expires = expires,
        lastUpdated = lastUpdated
    )

fun Int.toDccRevocationMode(): DccRevocationMode =
    when (this) {
        0 -> DccRevocationMode.COORDINATE
        1 -> DccRevocationMode.VECTOR
        2 -> DccRevocationMode.POINT
        else -> DccRevocationMode.UNKNOWN
    }

fun Int.toDccRevocationHashType(): DccRevocationHashType =
    when (this) {
        0 -> DccRevocationHashType.SIGNATURE
        1 -> DccRevocationHashType.UCI
        2 -> DccRevocationHashType.COUNTRYCODEUCI
        else -> DccRevocationHashType.UNKNOWN
    }

// TODO: review constants later