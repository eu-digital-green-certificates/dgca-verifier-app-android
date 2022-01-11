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
 *  Created by mykhailo.nester on 28/12/2021, 21:05
 */

package dcc.app.revocation.network.model

import com.google.gson.annotations.SerializedName

data class RevocationKIDData(

    @SerializedName("kid")
    val kid: String,

    @SerializedName("settings")
    val settings: List<RevocationSettings>
)

data class RevocationSettings(
    @SerializedName("mode")
    val mode: RevocationMode,

    @SerializedName("tag")
    val tag: String
)

enum class RevocationMode {
    coordinate,
    vector,
    point
}