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
 *  Created by osarapulov on 7/26/21 12:34 PM
 */

package dgca.verifier.app.android.data.local.rules

import dgca.verifier.app.engine.data.RuleIdentifier

fun RuleIdentifier.toRuleIdentifierLocal() = RuleIdentifierLocal(
    identifier = this.identifier,
    version = this.version,
    country = this.country,
    hash = this.hash
)

fun RuleIdentifierLocal.toRuleIdentifier() = RuleIdentifier(
    identifier = this.identifier,
    version = this.version,
    country = this.country,
    hash = this.hash
)