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
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
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
        viewModel.decodeResult.observe(viewLifecycleOwner) { handleDecodeResult(it) }
    }

    override fun viewModel(): BaseVerificationResultViewModel = viewModel

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams
    override fun timerView(): View = binding.timerView
    override fun actionButton(): Button = binding.actionButton
    override fun progressBar(): ProgressBar = binding.progressBar

    override fun qrCodeText(): String = args.qrCodeText
    override fun countryIsoCode(): String = args.countryIsoCode

    private fun handleDecodeResult(decodeResult: DecodeResult) {
        handleVerificationResult(decodeResult.verificationData)
        decodeResult.verificationError?.apply {
            setCertStatusError(this)
        }
    }

    private fun handleVerificationResult(verificationData: VerificationData) {
        setCertStatusUI(verificationData.getGeneralResult())
        setCertDataVisibility(verificationData.getGeneralResult())
        verificationData.certificateModel?.let { certificateModel ->
            binding.personFullName.text = certificateModel.getFullName()
            toggleButton(certificateModel)

            if (verificationData.getGeneralResult() != GeneralVerificationResult.FAILED) {
                showUserData(certificateModel)

                if (binding.greenCertificate.parent != null) {
                    when {
                        certificateModel.vaccinations?.size == 1 -> {
                            binding.greenCertificate.layoutResource =
                                R.layout.item_vaccination
                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                VaccinationViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.vaccinations.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                        certificateModel.recoveryStatements?.size == 1 -> {
                            binding.greenCertificate.layoutResource = R.layout.item_recovery

                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                RecoveryViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.recoveryStatements.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                        certificateModel.tests?.size == 1 -> {
                            binding.greenCertificate.layoutResource = R.layout.item_test

                            binding.greenCertificate.setOnInflateListener { stub, inflated ->
                                TestViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.tests.first())
                            }
                            binding.greenCertificate.inflate()
                        }
                    }
                }
            }
        }
    }

    private fun setCertStatusUI(generalVerificationResult: GeneralVerificationResult) {
        val text: String
        val imageId: Int
        val statusColor: ColorStateList
        val actionBtnText: String

        if (generalVerificationResult == GeneralVerificationResult.SUCCESS) {
            text = getString(R.string.cert_valid)
            imageId = R.drawable.check
            statusColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
            actionBtnText = getString(R.string.done)
        } else if (generalVerificationResult == GeneralVerificationResult.RULES_VALIDATION_FAILED) {
            text = getString(R.string.cert_limited_validity)
            imageId = R.drawable.check
            statusColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow))
            actionBtnText = getString(R.string.retry)
        } else {
            text = getString(R.string.cert_invalid)
            imageId = R.drawable.error
            statusColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            actionBtnText = getString(R.string.retry)
        }

        binding.status.text = text
        binding.certStatusIcon.setImageResource(imageId)
        binding.verificationStatusBg.backgroundTintList = statusColor
        actionButton().isVisible = true
        actionButton().backgroundTintList = statusColor
        actionButton().text = actionBtnText
        actionButton().isVisible = true
    }

    private fun setCertStatusError(verificationError: VerificationError) {
        binding.reasonForCertificateInvalidityTitle.visibility = View.VISIBLE
        binding.reasonForCertificateInvalidityName.visibility = View.VISIBLE
        binding.reasonForCertificateInvalidityName.text = getString(
            when (verificationError) {
                VerificationError.GREEN_CERTIFICATE_EXPIRED -> R.string.certificate_is_expired
                VerificationError.CERTIFICATE_REVOKED -> R.string.certificate_was_revoked
                VerificationError.VERIFICATION_FAILED -> R.string.verification_failed
                VerificationError.CERTIFICATE_EXPIRED -> R.string.signing_certificate_is_expired
                VerificationError.TEST_DATE_IS_IN_THE_FUTURE -> R.string.the_test_date_is_in_the_future
                VerificationError.TEST_RESULT_POSITIVE -> R.string.test_result_positive
                VerificationError.RECOVERY_NOT_VALID_SO_FAR -> R.string.recovery_not_valid_yet
                VerificationError.RECOVERY_NOT_VALID_ANYMORE -> R.string.recover_not_valid_anymore
                VerificationError.RULES_VALIDATION_FAILED -> R.string.rules_validation_failed
                VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
            }
        )
        if (verificationError == VerificationError.TEST_RESULT_POSITIVE) {
            binding.errorTestResult.visibility = View.VISIBLE
            binding.reasonTestResultValue.text = TestResult.DETECTED.value
        } else {
            binding.errorTestResult.visibility = View.GONE
        }

        if (verificationError == VerificationError.RULES_VALIDATION_FAILED) {
            val ruleValidationResultCards = mutableListOf<RuleValidationResultCard>()
            val context = requireContext()
            viewModel().validationResults.value?.forEach {
                ruleValidationResultCards.add(
                    it.toRuleValidationResultCard(context)
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

    private fun setCertDataVisibility(generalVerificationResult: GeneralVerificationResult) {
        binding.errorDetails.visibility =
            if (generalVerificationResult == GeneralVerificationResult.SUCCESS) View.GONE else View.VISIBLE
        if (generalVerificationResult == GeneralVerificationResult.SUCCESS) {
            binding.errorTestResult.visibility = View.GONE
        }
        binding.successDetails.visibility =
            if (generalVerificationResult != GeneralVerificationResult.FAILED) View.VISIBLE else View.GONE
    }

    private fun showUserData(certificate: CertificateModel) {
        binding.personStandardisedFamilyName.text = certificate.person.standardisedFamilyName
        binding.personStandardisedGivenName.text = certificate.person.standardisedGivenName
        if (certificate.person.standardisedGivenName?.isNotBlank() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            binding.personStandardisedGivenNameTitle.visibility = this
            binding.personStandardisedGivenName.visibility = this
        }

        binding.dateOfBirth.text =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)

        val dateOfBirth =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        if (dateOfBirth.isBlank()) {
            View.GONE
        } else {
            binding.dateOfBirth.text = dateOfBirth
            View.VISIBLE
        }.apply {
            binding.dateOfBirthTitle.visibility = this
            binding.dateOfBirth.visibility = this
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
}