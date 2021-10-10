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
 *  Created by osarapulov on 8/31/21 10:58 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.ViewDetailedVerificationResultBinding
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.verification.model.StandardizedVerificationResult
import dgca.verifier.app.android.verification.model.StandardizedVerificationResultCategory
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type


class DetailedVerificationResultView(context: Context, attrs: AttributeSet?) :
    CardView(context, attrs) {
    private val binding: ViewDetailedVerificationResultBinding =
        ViewDetailedVerificationResultBinding.inflate(LayoutInflater.from(context), this)

    init {
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            context.resources.getDimension(R.dimen.detailed_verification_result_banner_radius),
            context.resources.displayMetrics
        )
    }

    fun setUp(
        standardizedVerificationResult: StandardizedVerificationResult,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?
    ) {
        val techVerificationAsset =
            if (standardizedVerificationResult.category == StandardizedVerificationResultCategory.VALID
                || standardizedVerificationResult.category == StandardizedVerificationResultCategory.LIMITED_VALIDITY
            ) {
                R.drawable.ic_traffic_success
            } else {
                R.drawable.ic_traffic_fail
            }
        binding.techVerificationImage.setImageResource(techVerificationAsset)
        val (invalidationAsset, nonGeneralAcceptanceAsset, generalAcceptanceAsset) = ruleValidationResultModelsContainer?.getAssets()
            ?: Triple(
                R.drawable.ic_traffic_uncertain,
                R.drawable.ic_traffic_uncertain,
                R.drawable.ic_traffic_uncertain
            )

        binding.issuerInvalidationImage.setImageResource(invalidationAsset)
        binding.destinationAcceptanceImage.setImageResource(nonGeneralAcceptanceAsset)
        binding.travellerAcceptanceImage.setImageResource(generalAcceptanceAsset)
    }

    private fun RuleValidationResultModelsContainer.getAssets(): Triple<Int, Int, Int> {
        var invalidationAsset = R.drawable.ic_traffic_success
        var nonGeneralAcceptanceAsset = R.drawable.ic_traffic_success
        var generalAcceptanceAsset = R.drawable.ic_traffic_success
        this.ruleValidationResultModels.forEach { ruleValidationResultModel ->
            when {
                ruleValidationResultModel.result == Result.PASSED -> {
                }
                ruleValidationResultModel.rule.type == Type.INVALIDATION -> {
                    invalidationAsset =
                        if (invalidationAsset == R.drawable.ic_traffic_fail || ruleValidationResultModel.result == Result.FAIL) {
                            R.drawable.ic_traffic_fail
                        } else {
                            R.drawable.ic_traffic_uncertain
                        }
                }
                ruleValidationResultModel.rule.ruleCertificateType == RuleCertificateType.GENERAL -> {
                    generalAcceptanceAsset =
                        if (generalAcceptanceAsset == R.drawable.ic_traffic_fail || ruleValidationResultModel.result == Result.FAIL) {
                            R.drawable.ic_traffic_fail
                        } else {
                            R.drawable.ic_traffic_uncertain
                        }
                }
                else -> {
                    nonGeneralAcceptanceAsset =
                        if (nonGeneralAcceptanceAsset == R.drawable.ic_traffic_fail || ruleValidationResultModel.result == Result.FAIL) {
                            R.drawable.ic_traffic_fail
                        } else {
                            R.drawable.ic_traffic_uncertain
                        }
                }
            }

        }
        return Triple(
            invalidationAsset,
            nonGeneralAcceptanceAsset,
            generalAcceptanceAsset
        )
    }
}