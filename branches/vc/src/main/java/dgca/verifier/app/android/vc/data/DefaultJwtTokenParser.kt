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

class DefaultJwtTokenParser : JwtTokenParser {

    override fun parse(jwtToken: String): JwtObject {
        val tokens = jwtToken.split('.')
        val header = String(tokens[0].fromBase64())
        val body = String(tokens[1].fromBase64())
        return JwtObject(header, body)
    }
}