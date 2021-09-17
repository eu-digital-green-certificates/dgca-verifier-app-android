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
 *  Created by mykhailo.nester on 4/24/21 2:10 PM
 */

package dgca.verifier.app.android.verification

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.verifier.app.android.R
import dgca.verifier.app.android.YEAR_MONTH_DAY
import dgca.verifier.app.android.databinding.DialogFragmentVerificationResultBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.TestResult
import dgca.verifier.app.android.parseFromTo
import dgca.verifier.app.android.verification.certs.RecoveryViewHolder
import dgca.verifier.app.android.verification.certs.TestViewHolder
import dgca.verifier.app.android.verification.certs.VaccinationViewHolder
import dgca.verifier.app.android.verification.rules.RuleValidationResultCard
import dgca.verifier.app.android.verification.rules.RuleValidationResultsAdapter
import dgca.verifier.app.android.verification.rules.toRuleValidationResultCard

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationResultDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentVerificationResultBinding>() {
    private val hideLiveData: MutableLiveData<Void?> = MutableLiveData()

    private val viewModel by viewModels<VerificationResultResultViewModel>()
    private val args by navArgs<VerificationResultDialogFragmentArgs>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentVerificationResultBinding =
        DialogFragmentVerificationResultBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rulesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        handleDecodeResult()

        hideLiveData.observe(viewLifecycleOwner, {
            dismiss()
        })

        startTimer()
    }

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams
    override fun timerView(): View = binding.timerView
    override fun actionButton(): Button = binding.actionButton

    private fun handleDecodeResult() {
        binding.verificationResultHeaderView.setUp(args.standardizedVerificationResult, args.certificateModel, args.ruleValidationResultModelsContainer)
        handleVerificationResult(
            args.certificateModel,
            args.standardizedVerificationResult.category
        )
        if (args.standardizedVerificationResult.category != StandardizedVerificationResultCategory.VALID) {
            setCertStatusError(args.standardizedVerificationResult)
        }
    }

    private fun handleVerificationResult(
        certificateModel: CertificateModel?,
        standardizedVerificationResultCategory: StandardizedVerificationResultCategory
    ) {
        setCertStatusUI(standardizedVerificationResultCategory)
        setCertDataVisibility(standardizedVerificationResultCategory)
        certificateModel?.let { it ->
            toggleButton(it)

            if (standardizedVerificationResultCategory != StandardizedVerificationResultCategory.INVALID) {
                showUserData(it)

                if (binding.greenCertificate.parent != null) {
                    when {
                        it.vaccinations?.size == 1 -> {
                            binding.greenCertificate.layoutResource =
                                R.layout.item_vaccination
                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                VaccinationViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(it.vaccinations.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                        it.recoveryStatements?.size == 1 -> {
                            binding.greenCertificate.layoutResource = R.layout.item_recovery

                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                RecoveryViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(it.recoveryStatements.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                        it.tests?.size == 1 -> {
                            binding.greenCertificate.layoutResource = R.layout.item_test

                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                TestViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(it.tests.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                    }
                }
            }
        }
    }

    private fun setCertStatusUI(standardizedVerificationResultCategory: StandardizedVerificationResultCategory) {
        val text: String
        val imageId: Int
        val statusColor: ColorStateList
        val actionBtnText: String

        when (standardizedVerificationResultCategory) {
            StandardizedVerificationResultCategory.VALID -> {
                text = getString(R.string.cert_valid)
                imageId = R.drawable.check
                statusColor =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
                actionBtnText = getString(R.string.done)
            }
            StandardizedVerificationResultCategory.LIMITED_VALIDITY -> {
                text = getString(R.string.cert_limited_validity)
                imageId = R.drawable.check
                statusColor =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow))
                actionBtnText = getString(R.string.retry)
            }
            StandardizedVerificationResultCategory.INVALID -> {
                text = getString(R.string.cert_invalid)
                imageId = R.drawable.error
                statusColor =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
                actionBtnText = getString(R.string.retry)
            }
        }

        actionButton().isVisible = true
        actionButton().backgroundTintList = statusColor
        actionButton().text = actionBtnText
        actionButton().isVisible = true
    }

    private fun setCertStatusError(standardizedVerificationResult: StandardizedVerificationResult) {
        binding.reasonForCertificateInvalidityTitle.visibility = View.VISIBLE
        binding.reasonForCertificateInvalidityName.visibility = View.VISIBLE
        binding.reasonForCertificateInvalidityName.text = getString(
            when (standardizedVerificationResult) {
                StandardizedVerificationResult.GREEN_CERTIFICATE_EXPIRED -> R.string.certificate_is_expired
                StandardizedVerificationResult.CERTIFICATE_REVOKED -> R.string.certificate_was_revoked
                StandardizedVerificationResult.VERIFICATION_FAILED -> R.string.verification_failed
                StandardizedVerificationResult.CERTIFICATE_EXPIRED -> R.string.signing_certificate_is_expired
                StandardizedVerificationResult.VACCINATION_DATE_IS_IN_THE_FUTURE -> R.string.the_vaccination_date_is_in_the_future
                StandardizedVerificationResult.TEST_DATE_IS_IN_THE_FUTURE -> R.string.the_test_date_is_in_the_future
                StandardizedVerificationResult.TEST_RESULT_POSITIVE -> R.string.test_result_positive
                StandardizedVerificationResult.RECOVERY_NOT_VALID_SO_FAR -> R.string.recovery_not_valid_yet
                StandardizedVerificationResult.RECOVERY_NOT_VALID_ANYMORE -> R.string.recover_not_valid_anymore
                StandardizedVerificationResult.RULES_VALIDATION_FAILED -> R.string.rules_validation_failed
                StandardizedVerificationResult.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
                else -> throw IllegalArgumentException()
            }
        )
        if (standardizedVerificationResult == StandardizedVerificationResult.TEST_RESULT_POSITIVE) {
            binding.errorTestResult.visibility = View.VISIBLE
            binding.reasonTestResultValue.text = TestResult.DETECTED.value
        } else {
            binding.errorTestResult.visibility = View.GONE
        }

        if (standardizedVerificationResult == StandardizedVerificationResult.RULES_VALIDATION_FAILED) {
            val ruleValidationResultCards = mutableListOf<RuleValidationResultCard>()
            args.ruleValidationResultModelsContainer?.ruleValidationResultModels?.forEach {
                ruleValidationResultCards.add(
                    it.toRuleValidationResultCard()
                )
            }
            binding.rulesList.adapter =
                RuleValidationResultsAdapter(layoutInflater, ruleValidationResultCards)
            binding.reasonForCertificateInvalidityName.setOnClickListener {
                binding.rulesList.visibility =
                    if (binding.rulesList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                binding.reasonForCertificateInvalidityName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(
                        resources,
                        if (binding.rulesList.visibility == View.VISIBLE) R.drawable.icon_collapsed else R.drawable.icon_expanded,
                        null
                    ),
                    null
                )
            }
            binding.reasonForCertificateInvalidityTitle.text =
                getString(R.string.possible_limitation)
            val outValue = TypedValue()
            requireContext().theme.resolveAttribute(
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
    }

    private fun setCertDataVisibility(standardizedVerificationResultCategory: StandardizedVerificationResultCategory) {
        binding.errorDetails.visibility =
            if (standardizedVerificationResultCategory == StandardizedVerificationResultCategory.VALID) View.GONE else View.VISIBLE
        if (standardizedVerificationResultCategory == StandardizedVerificationResultCategory.VALID) {
            binding.errorTestResult.visibility = View.GONE
        }
    }

    private fun showUserData(certificate: CertificateModel) {
        binding.personStandardisedFamilyName.text = certificate.person.standardisedFamilyName
        binding.personStandardisedFamilyNameTitle.visibility = View.VISIBLE
        binding.personStandardisedFamilyName.visibility = View.VISIBLE
        binding.personStandardisedGivenName.text = certificate.person.standardisedGivenName
        if (certificate.person.standardisedGivenName?.isNotBlank() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            binding.personStandardisedGivenNameTitle.visibility = this
            binding.personStandardisedGivenName.visibility = this
        }

        binding.dateOfBirthValue.text =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)

        val dateOfBirth =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        if (dateOfBirth.isBlank()) {
            View.GONE
        } else {
            binding.dateOfBirthValue.text = dateOfBirth
            View.VISIBLE
        }.apply {
            binding.dateOfBirthTitle.visibility = this
            binding.dateOfBirthValue.visibility = this
        }
    }

    private fun toggleButton(certificate: CertificateModel) {
        binding.certificateTypeText.text = when {
            certificate.vaccinations?.isNotEmpty() == true -> getString(
                R.string.type_vaccination,
                certificate.vaccinations.first().doseNumber,
                certificate.vaccinations.first().totalSeriesOfDoses
            )
            certificate.recoveryStatements?.isNotEmpty() == true -> getString(R.string.type_recovered)
            certificate.tests?.isNotEmpty() == true -> getString(R.string.type_test)
            else -> getString(R.string.type_test)
        }
        binding.generalInfo.visibility = View.VISIBLE
    }

    private fun startTimer() {
        binding.timerView.animate()
            .setDuration(COLLAPSE_TIME)
            .translationX(0F)
            .withEndAction {
                hideLiveData.value = null
            }
            .start()
    }

    companion object {
        private const val COLLAPSE_TIME = 15000L // 15 sec
    }
}