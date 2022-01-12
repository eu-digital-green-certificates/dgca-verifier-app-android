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

package dcc.app.revocation.network.mapper

import dcc.app.revocation.domain.model.DccRevocationHashType
import dcc.app.revocation.domain.model.DccRevocationMode
import dcc.app.revocation.domain.model.RevocationKidData
import dcc.app.revocation.domain.model.RevocationSettingsData
import dcc.app.revocation.network.model.HashType
import dcc.app.revocation.network.model.RevocationKIDResponse
import dcc.app.revocation.network.model.RevocationMode
import dcc.app.revocation.network.model.RevocationSettings

fun RevocationKIDResponse.toRevocationKidData(): RevocationKidData =
    RevocationKidData(
        kid = kid,
        settings = this.settings.map { it.toSettingsData() }
    )

fun RevocationSettings.toSettingsData(): RevocationSettingsData =
    RevocationSettingsData(
        mode = mode.toDccRevocationMode(),
        hashType = hashType.toDccRevocationHashType(),
        tag = tag,
        lastUpdated = lastUpdated
    )

fun RevocationMode.toDccRevocationMode(): DccRevocationMode =
    when (this) {
        RevocationMode.COORDINATE -> DccRevocationMode.COORDINATE
        RevocationMode.VECTOR -> DccRevocationMode.VECTOR
        RevocationMode.POINT -> DccRevocationMode.POINT
    }

fun HashType.toDccRevocationHashType(): DccRevocationHashType =
    when (this) {
        HashType.SIGNATURE -> DccRevocationHashType.SIGNATURE
        HashType.UCI -> DccRevocationHashType.UCI
        HashType.COUNTRYCODEUCI -> DccRevocationHashType.COUNTRYCODEUCI
    }