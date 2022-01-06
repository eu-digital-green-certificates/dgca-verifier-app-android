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
 *  Created by osarapulov on 12/27/21, 9:55 PM
 */

package dcc.app.revocation.data.source

import java.time.ZonedDateTime

interface DccRevocationRepository {
    fun add(kid: String, dccRevocationHash: String, dccRevocationExpirationDate: ZonedDateTime)

    fun add(kid: String, dccRevocationEntry: DccRevocationEntry)

    fun contains(kid: String, dccHash: String): Boolean

    fun remove(kid: String, dccHash: String)
}