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
 *  Created by mykhailo.nester on 25/03/2022, 23:15
 */

package dgca.verifier.app.android.vc.data

import dgca.verifier.app.android.vc.fromBase64
import dgca.verifier.app.android.vc.fromBase64Url
import dgca.verifier.app.android.vc.inflate
import timber.log.Timber

class DefaultJwsTokenParser : JwsTokenParser {

    override fun parse(jwsToken: String): JwsObject? {
        val token = jwsToken.removePrefix("shc:/") // TODO: clarify numeric decoding

        return try {
            val tokens = jwsToken.split('.')
            val header = String(tokens[0].fromBase64())
            val body = inflate(tokens[1].fromBase64Url()).toString(Charsets.UTF_8)
            val signature = String(tokens[2].fromBase64())
            JwsObject(header, body, signature)

        } catch (ex: Exception) {
            Timber.e(ex, "Invalid JWS structure")
            null
        }
    }
}