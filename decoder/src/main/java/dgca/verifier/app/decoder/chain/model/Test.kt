/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by Mykhailo Nester on 4/23/21 9:51 AM
 */

package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Test(

    @JsonProperty("dis")
    val disease: String,

    @JsonProperty("typ")
    val type: String,

    @JsonProperty("tma")
    val manufacturer: String? = null,

    @JsonProperty("ori")
    val sampleOrigin: String? = null,

    @JsonProperty("dts")
    val dateTimeSample: String,

    @JsonProperty("dtr")
    val dateTimeResult: String,

    @JsonProperty("res")
    val resultPositive: Boolean,

    @JsonProperty("fac")
    val testFacility: String,

    @JsonProperty("cou")
    val country: String
)