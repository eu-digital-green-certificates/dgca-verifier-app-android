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
 *  Created by osarapulov on 9/3/21 7:57 AM
 */

package dgca.verifier.app.android.model.rules

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.Rule

fun Rule.toRuleModel(): RuleModel {
    return RuleModel(
        identifier = this.identifier,
        type = this.type,
        version = this.version,
        schemaVersion = this.schemaVersion,
        engine = this.engine,
        engineVersion = this.engineVersion,
        ruleCertificateType = this.ruleCertificateType,
        descriptions = this.descriptions,
        validFrom = this.validFrom,
        validTo = this.validTo,
        affectedString = this.affectedString,
        logic = this.logic.asText(),
        countryCode = this.countryCode,
        region = this.region
    )
}

fun RuleModel.toRule(objectMapper: ObjectMapper): Rule {
    return Rule(
        identifier = this.identifier,
        type = this.type,
        version = this.version,
        schemaVersion = this.schemaVersion,
        engine = this.engine,
        engineVersion = this.engineVersion,
        ruleCertificateType = this.ruleCertificateType,
        descriptions = this.descriptions,
        validFrom = this.validFrom,
        validTo = this.validTo,
        affectedString = this.affectedString,
        logic = objectMapper.readValue(this.logic, JsonNode::class.java),
        countryCode = this.countryCode,
        region = this.region
    )
}

fun ValidationResult.toRuleValidationResultModel(): RuleValidationResultModel {
    return RuleValidationResultModel(
        rule = this.rule.toRuleModel(),
        result = this.result,
        current = this.current,
        validationErrors = this.validationErrors
    )
}

fun RuleValidationResultModel.toRuleValidationResult(objectMapper: ObjectMapper): ValidationResult {
    return ValidationResult(
        rule = this.rule.toRule(objectMapper),
        result = this.result,
        current = this.current,
        validationErrors = this.validationErrors
    )
}

fun List<ValidationResult>.toRuleValidationResultModels(): List<RuleValidationResultModel> {
    return this.map { it.toRuleValidationResultModel() }
}

fun List<RuleValidationResultModel>.toRuleValidationResults(objectMapper: ObjectMapper): List<ValidationResult> {
    return this.map { it.toRuleValidationResult(objectMapper) }
}
