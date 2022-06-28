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
 *  Created by osarapulov on 3/17/22, 2:27 PM
 */

package dgca.verifier.app.android.dcc.ui.verification.detailed

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.android.app.dcc.R
import com.android.app.dcc.databinding.ViewDetailedVerificationResultHeaderBinding
import dgca.verifier.app.android.dcc.model.CertificateModel
import dgca.verifier.app.android.dcc.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.dcc.ui.verification.model.StandardizedVerificationResult
import dgca.verifier.app.android.dcc.ui.verification.model.StandardizedVerificationResultCategory

class DetailedVerificationResultHeaderView(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    private val binding: ViewDetailedVerificationResultHeaderBinding =
        ViewDetailedVerificationResultHeaderBinding.inflate(LayoutInflater.from(context), this)

    fun setUp(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?
    ) {
        binding.personFullName.text = certificateModel?.getFullName() ?: ""

        val isValid =
            standardizedVerificationResult.category == StandardizedVerificationResultCategory.VALID

        val (colorRes, textRes) = when (standardizedVerificationResult.category) {
            StandardizedVerificationResultCategory.VALID -> Pair(R.color.green, R.string.cert_valid)
            StandardizedVerificationResultCategory.INVALID -> Pair(
                R.color.red,
                R.string.cert_invalid
            )
            StandardizedVerificationResultCategory.LIMITED_VALIDITY -> Pair(
                R.color.yellow,
                R.string.cert_limited_validity
            )
        }
        binding.status.text = context.getString(textRes)
        binding.verificationStatusBackground.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))

        if (isValid) {
            binding.detailedVerificationResultView.visibility = View.GONE
            binding.certStatusIcon.visibility = View.VISIBLE
            View.GONE
        } else {
            binding.detailedVerificationResultView.visibility = View.VISIBLE
            binding.certStatusIcon.visibility = View.GONE
            binding.detailedVerificationResultView.setUp(
                standardizedVerificationResult,
                ruleValidationResultModelsContainer
            )
            View.VISIBLE
        }
    }
}
