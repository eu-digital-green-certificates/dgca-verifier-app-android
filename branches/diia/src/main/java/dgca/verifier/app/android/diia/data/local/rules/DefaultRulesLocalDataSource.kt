/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
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
 *  Created by osarapulov on 7/26/21 12:33 PM
 */

package dgca.verifier.app.android.diia.data.local.rules

import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.RuleIdentifier
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.data.source.local.rules.RulesLocalDataSource
import java.time.ZonedDateTime

class DefaultRulesLocalDataSource(private val rulesDao: RulesDao) : RulesLocalDataSource {

    override fun addRules(ruleIdentifiers: Collection<RuleIdentifier>, rules: Collection<Rule>) {
        rulesDao.insertRulesData(
            ruleIdentifiers.map { it.toRuleIdentifierLocal() },
            rules.map { it.toRuleWithDescriptionLocal() })
    }

    override fun removeRulesBy(identifiers: Collection<String>) {
        rulesDao.deleteRulesDataBy(identifiers)
    }

    override fun getRuleIdentifiers(): List<RuleIdentifier> =
        rulesDao.getRuleIdentifiers().map { it.toRuleIdentifier() }

    override fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule> = rulesDao.getRulesWithDescriptionsBy(
        countryIsoCode,
        validationClock,
        type,
        ruleCertificateType,
        RuleCertificateType.GENERAL
    ).toRules()
}
