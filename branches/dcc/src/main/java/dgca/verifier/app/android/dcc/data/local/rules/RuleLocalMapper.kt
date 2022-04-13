/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 2:24 PM
 */

package dgca.verifier.app.android.dcc.data.local.rules

import dgca.verifier.app.android.dcc.data.local.model.DescriptionLocal
import dgca.verifier.app.android.dcc.data.local.model.RuleLocal
import dgca.verifier.app.android.dcc.data.local.model.RuleWithDescriptionsLocal
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.Rule
import java.util.*

fun Rule.toRuleWithDescriptionLocal(): RuleWithDescriptionsLocal =
    RuleWithDescriptionsLocal(toRuleLocal(), descriptions.toDescriptionsLocal())

fun Rule.toRuleLocal(): RuleLocal =
    RuleLocal(
        identifier = identifier,
        type = type,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        ruleCertificateType = ruleCertificateType,
        validFrom = validFrom.withZoneSameInstant(UTC_ZONE_ID),
        validTo = validTo.withZoneSameInstant(UTC_ZONE_ID),
        affectedString = affectedString,
        logic = logic,
        countryCode = countryCode,
        region = region
    )

fun Map<String, String>.toDescriptionsLocal(): List<DescriptionLocal> {
    val descriptionsLocal = mutableListOf<DescriptionLocal>()
    forEach { descriptionsLocal.add(DescriptionLocal(lang = it.key, desc = it.value)) }

    return descriptionsLocal
}

fun List<DescriptionLocal>.toDescriptions(): Map<String, String> {
    val descriptions = mutableMapOf<String, String>()
    forEach { descriptions[it.lang.toLowerCase(Locale.ROOT)] = it.desc }

    return descriptions
}

fun RuleWithDescriptionsLocal.toRule(): Rule =
    Rule(
        identifier = rule.identifier,
        type = rule.type,
        version = rule.version,
        schemaVersion = rule.schemaVersion,
        engine = rule.engine,
        engineVersion = rule.engineVersion,
        ruleCertificateType = rule.ruleCertificateType,
        validFrom = rule.validFrom.withZoneSameInstant(UTC_ZONE_ID),
        validTo = rule.validTo.withZoneSameInstant(UTC_ZONE_ID),
        affectedString = rule.affectedString,
        logic = rule.logic,
        countryCode = rule.countryCode,
        descriptions = descriptions.toDescriptions(),
        region = rule.region
    )

fun List<RuleWithDescriptionsLocal>.toRules(): List<Rule> = map { it.toRule() }
