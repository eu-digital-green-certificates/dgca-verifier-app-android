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
 *  Created by osarapulov on 9/1/21 7:24 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import dgca.verifier.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.verifier.app.android.R
import dgca.verifier.app.android.YEAR_MONTH_DAY
import dgca.verifier.app.android.databinding.ViewDetailedCertificateViewBinding
import dgca.verifier.app.android.model.*
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.parseFromTo
import dgca.verifier.app.android.verification.StandardizedVerificationResult
import dgca.verifier.app.android.verification.StandardizedVerificationResultCategory
import dgca.verifier.app.android.verification.certs.RecoveryViewHolder
import dgca.verifier.app.android.verification.certs.TestViewHolder
import dgca.verifier.app.android.verification.certs.VaccinationViewHolder
import dgca.verifier.app.android.verification.rules.RuleValidationResultCard
import dgca.verifier.app.android.verification.rules.RuleValidationResultsAdapter
import dgca.verifier.app.android.verification.rules.toRuleValidationResultCard

class DetailedCertificateView(context: Context, attrs: AttributeSet?) :
    MaterialCardView(context, attrs) {
    private val binding: ViewDetailedCertificateViewBinding =
        ViewDetailedCertificateViewBinding.inflate(LayoutInflater.from(context), this)
    private var isExpanded = false
    private var isRulesListExpanded = true
    private lateinit var data: Triple<CertificateModel, StandardizedVerificationResult, RuleValidationResultModelsContainer?>

    init {
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            context.resources.getDimension(R.dimen.detailed_verification_result_banner_radius),
            context.resources.displayMetrics
        )
        strokeWidth = resources.getDimensionPixelSize(R.dimen.default_stroke_width)
        setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)))

        binding.expandButton.setOnClickListener {
            setExpanded(!isExpanded)
        }
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        binding.expandButton.setImageResource(if (expanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
        setUp(data)
    }

    private fun setUpPersonData(personModel: PersonModel) {
        binding.personStandardisedFamilyName.text = personModel.standardisedFamilyName
        binding.personStandardisedGivenName.text = personModel.standardisedGivenName
    }

    private fun setUpVaccination(vaccinationList: List<VaccinationModel>) {
        binding.greenCertificate.layoutResource = R.layout.item_vaccination
        binding.greenCertificate.setOnInflateListener { stub, inflated ->
            VaccinationViewHolder.create(
                inflated as ViewGroup
            ).bind(vaccinationList.first())
        }
    }

    private fun setUpRecovery(recoveryList: List<RecoveryModel>) {
        binding.greenCertificate.layoutResource = R.layout.item_recovery
        binding.greenCertificate.setOnInflateListener { stub, inflated ->
            RecoveryViewHolder.create(
                inflated as ViewGroup
            ).bind(recoveryList.first())
        }
    }

    private fun setUpTest(testList: List<TestModel>) {
        binding.greenCertificate.layoutResource = R.layout.item_test
        binding.greenCertificate.setOnInflateListener { stub, inflated ->
            TestViewHolder.create(
                inflated as ViewGroup
            ).bind(testList.first())
        }
    }

    private fun setUpGreenCertificateData(certificateModel: CertificateModel) {
        if (binding.greenCertificate.parent != null) {
            when {
                certificateModel.vaccinations?.size == 1 -> {
                    setUpVaccination(certificateModel.vaccinations)
                }
                certificateModel.recoveryStatements?.size == 1 -> {
                    setUpRecovery(certificateModel.recoveryStatements)
                }
                certificateModel.tests?.size == 1 -> {
                    setUpTest(certificateModel.tests)
                }
            }
            binding.greenCertificate.inflate()
        }
    }

    private fun setUpErrorType(standardizedVerificationResult: StandardizedVerificationResult) {
        binding.reasonForCertificateInvalidityName.text = context.getString(
            when (standardizedVerificationResult) {
                StandardizedVerificationResult.GREEN_CERTIFICATE_EXPIRED -> R.string.certificate_is_expired
                StandardizedVerificationResult.CERTIFICATE_REVOKED -> R.string.certificate_was_revoked
                StandardizedVerificationResult.VERIFICATION_FAILED -> R.string.verification_failed
                StandardizedVerificationResult.CERTIFICATE_EXPIRED -> R.string.signing_certificate_is_expired
                StandardizedVerificationResult.TEST_DATE_IS_IN_THE_FUTURE -> R.string.the_test_date_is_in_the_future
                StandardizedVerificationResult.TEST_RESULT_POSITIVE -> R.string.test_result_positive
                StandardizedVerificationResult.RECOVERY_NOT_VALID_SO_FAR -> R.string.recovery_not_valid_yet
                StandardizedVerificationResult.RECOVERY_NOT_VALID_ANYMORE -> R.string.recover_not_valid_anymore
                StandardizedVerificationResult.RULES_VALIDATION_FAILED -> R.string.rules_validation_failed
                StandardizedVerificationResult.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
                else -> throw IllegalArgumentException()
            }
        )
    }

    private fun setUpRules(rulesValidationResultModelsContainer: RuleValidationResultModelsContainer) {
        val ruleValidationResultCards = mutableListOf<RuleValidationResultCard>()
        rulesValidationResultModelsContainer.ruleValidationResultModels.forEach {
            ruleValidationResultCards.add(
                it.toRuleValidationResultCard()
            )
        }
        binding.rulesList.apply {
            adapter = RuleValidationResultsAdapter(
                LayoutInflater.from(context),
                ruleValidationResultCards
            )
            layoutManager = LinearLayoutManager(context)
        }

        binding.reasonForCertificateInvalidityName.setOnClickListener {
            isRulesListExpanded = !isRulesListExpanded
            binding.rulesList.visibility =
                if (isRulesListExpanded) View.GONE else View.VISIBLE
            binding.reasonForCertificateInvalidityName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ResourcesCompat.getDrawable(
                    resources,
                    if (isRulesListExpanded) R.drawable.icon_collapsed else R.drawable.icon_expanded,
                    null
                ),
                null
            )
        }
        binding.reasonForCertificateInvalidityTitle.text =
            context.getString(R.string.possible_limitation)
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true
        )
        binding.reasonForCertificateInvalidityName.setBackgroundResource(outValue.resourceId)
        binding.reasonForCertificateInvalidityName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.icon_expanded, null),
            null
        )
    }

    fun setCertificateModel(
        certificateModel: CertificateModel,
        standardizedVerificationResult: StandardizedVerificationResult,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?
    ) {
        setUpPersonData(certificateModel.person)
        setUpGreenCertificateData(certificateModel)
        if (standardizedVerificationResult.category != StandardizedVerificationResultCategory.VALID) {
            setUpErrorType(standardizedVerificationResult)
        }
        if (standardizedVerificationResult == StandardizedVerificationResult.TEST_RESULT_POSITIVE) {
            binding.testResultValue.text = TestResult.DETECTED.value
        }

        if (standardizedVerificationResult == StandardizedVerificationResult.RULES_VALIDATION_FAILED && ruleValidationResultModelsContainer?.ruleValidationResultModels?.isNotEmpty() == true) {
            setUpRules(ruleValidationResultModelsContainer)
        }

        setExpanded(true)
    }

    private fun setUp(data: Triple<CertificateModel, StandardizedVerificationResult, RuleValidationResultModelsContainer?>) {
        val certificateModel = data.first
        val standardizedVerificationResult: StandardizedVerificationResult =
            data.second
        setUpCertificateType(
            standardizedVerificationResult.category,
            certificateModel
        )
        setCertStatusError(standardizedVerificationResult)
    }

    private fun showUserData(certificate: CertificateModel) {
        if (isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            binding.personStandardisedFamilyNameTitle.visibility = this
            binding.personStandardisedFamilyName.visibility = this
        }

        if (isExpanded && certificate.person.standardisedGivenName?.isNotBlank() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            binding.personStandardisedGivenNameTitle.visibility = this
            binding.personStandardisedGivenName.visibility = this
        }

        certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
            .let { birthday ->
                if (birthday.isNotBlank() && isExpanded) {
                    binding.dateOfBirthValue.text = birthday
                    View.VISIBLE
                } else {
                    View.GONE
                }.apply {
                    binding.dateOfBirthTitle.visibility = this
                    binding.dateOfBirthValue.visibility = this
                }
            }
    }

    private fun setUpCertificateType(
        standardizedVerificationResultCategory: StandardizedVerificationResultCategory,
        certificateModel: CertificateModel
    ) {
        if (standardizedVerificationResultCategory != StandardizedVerificationResultCategory.INVALID) {
            showUserData(certificateModel)
        }
        binding.greenCertificate.visibility =
            if (isExpanded && standardizedVerificationResultCategory != StandardizedVerificationResultCategory.INVALID) View.VISIBLE else View.GONE
    }

    private fun setCertStatusError(
        standardizedVerificationResult: StandardizedVerificationResult,
    ) {
        if (standardizedVerificationResult.category != StandardizedVerificationResultCategory.VALID && isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }.let { visibility ->
            binding.reasonForCertificateInvalidityTitle.visibility = visibility
            binding.reasonForCertificateInvalidityName.visibility = visibility
        }


        if (standardizedVerificationResult == StandardizedVerificationResult.TEST_RESULT_POSITIVE && isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }.let { visibility ->
            binding.errorTestResult.visibility = visibility
        }

        if (standardizedVerificationResult == StandardizedVerificationResult.RULES_VALIDATION_FAILED && isExpanded && isRulesListExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }.let { visibility ->
            binding.rulesList.visibility = visibility
        }
    }
}