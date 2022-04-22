/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 05/04/2022, 11:18
 */

package dgca.verifier.app.android.vc.data.remote.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Jwk(
    @JsonProperty("kty")
    val kty: String,
    @JsonProperty("kid")
    val kid: String,
    @JsonProperty("use")
    val use: String?,
    @JsonProperty("alg")
    val alg: String?,
    @JsonProperty("x5c")
    val x5c: List<String>?,
    @JsonProperty("crv")
    val crv: String?,
    @JsonProperty("x")
    val x: String?,
    @JsonProperty("y")
    val y: String?,
    @JsonProperty("crlVersion")
    val crlVersion: Long?
)