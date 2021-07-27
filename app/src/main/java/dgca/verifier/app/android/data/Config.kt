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
 *  Created by osarapulov on 5/17/21 8:17 AM
 */

package dgca.verifier.app.android.data

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Holds information of public key info hashes for certificate pinning
 * and private policy url info.
 */
data class Config(
    @JsonProperty("origin") val origin: String?,
    @JsonProperty("versions") val versions: Map<String, Version>?,
) {

    private companion object {
        const val DEFAULT_VERSION_NAME = "default"
        const val STATUS_ENDPOINT_NAME = "status"
        const val UPDATE_ENDPOINT_NAME = "update"
        const val COUNTRY_LIST_ENDPOINT_NAME = "countryList"
        const val RULES_ENDPOINT_NAME = "rules"
        const val VALUE_SETS_ENDPOINT_NAME = "valuesets"
    }

    private fun getCurrentVersionOrUseDefault(versionName: String): Version? =
        versions?.get(versionName) ?: versions?.get(DEFAULT_VERSION_NAME)

    fun getContextUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.contextEndpoint?.url ?: ""

    fun getStatusUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.endpoints?.get(STATUS_ENDPOINT_NAME)?.url ?: ""

    fun getUpdateUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.endpoints?.get(UPDATE_ENDPOINT_NAME)?.url ?: ""

    fun getCountriesUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.endpoints?.get(COUNTRY_LIST_ENDPOINT_NAME)?.url
            ?: ""

    fun getRulesUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.endpoints?.get(RULES_ENDPOINT_NAME)?.url ?: ""

    fun getValueSetsUrl(versionName: String): String =
        getCurrentVersionOrUseDefault(versionName)?.endpoints?.get(VALUE_SETS_ENDPOINT_NAME)?.url
            ?: ""
}

data class Endpoint(

    @JsonProperty("url")
    val url: String?,

    @JsonProperty("pubKeys")
    val pubKeys: Collection<String>?
)

data class Version(

    @JsonProperty("privacyUrl")
    val privacyUrl: String?,

    @JsonProperty("context")
    val contextEndpoint: Endpoint?,

    @JsonProperty("outdated")
    val outdated: Boolean?,

    @JsonProperty("endpoints")
    val endpoints: Map<String, Endpoint>?
)