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


import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.*
import dgca.verifier.app.android.databinding.DialogFragmentVerificationBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.TestResult
import dgca.verifier.app.android.verification.certs.RecoveryViewHolder
import dgca.verifier.app.android.verification.certs.TestViewHolder
import dgca.verifier.app.android.verification.certs.VaccinationViewHolder
import dgca.verifier.app.android.verification.rules.RuleValidationResultCard
import dgca.verifier.app.android.verification.rules.RuleValidationResultsAdapter
import dgca.verifier.app.android.verification.rules.toRuleValidationResultCard


@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationDialogFragment : BottomSheetDialogFragment() {

    private val args by navArgs<VerificationDialogFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

    private var _binding: DialogFragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private val hideLiveData: MutableLiveData<Void?> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val params = binding.content.layoutParams as FrameLayout.LayoutParams
        params.height = height - TOP_MARGIN.dpToPx()

        binding.timerView.translationX = -displayMetrics.widthPixels.toFloat()

        dialog.expand()

        hideLiveData.observe(viewLifecycleOwner, {
            dismiss()
        })

        binding.rulesList.layoutManager = LinearLayoutManager(requireContext())
        binding.actionBtn.setOnClickListener { dismiss() }

        viewModel.verificationData.observe(viewLifecycleOwner, { verificationData ->
            if (verificationData.verificationResult == null) {
                hideLiveData.value = null
            } else {
                handleVerificationResult(verificationData)
            }
        })
        viewModel.verificationError.observe(viewLifecycleOwner, {
            setCertStatusError(it)
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressBar.isVisible = it
        })

        viewModel.init(args.qrCodeText, args.countryIsoCode)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleVerificationResult(verificationData: VerificationData) {
        setCertStatusUI(verificationData.getGeneralResult())
        setCertDataVisibility(verificationData.getGeneralResult())
        verificationData.certificateModel?.let { certificateModel ->
            binding.personFullName.text = certificateModel.getFullName()
            toggleButton(certificateModel)


            // TODO remove before release
            if (verificationData.getGeneralResult() == GeneralVerificationResult.SUCCESS) {
                val ruleValidationResultCards = mutableListOf<RuleValidationResultCard>()
                val context = requireContext()
                binding.rulesList.visibility = View.VISIBLE
                viewModel.validationResults.value?.forEach { validationResult ->
                    ruleValidationResultCards.add(
                        validationResult.toRuleValidationResultCard(context)
                    )
                }
                binding.rulesList.adapter =
                    RuleValidationResultsAdapter(layoutInflater, ruleValidationResultCards)
            }

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
        startTimer()
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
        binding.actionBtn.isVisible = true
        binding.actionBtn.backgroundTintList = statusColor
        binding.actionBtn.text = actionBtnText
        binding.actionBtn.isVisible = true
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
            viewModel.validationResults.value?.forEach {
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
        binding.sucessDetails.visibility =
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
        private const val TOP_MARGIN = 50
        private const val COLLAPSE_TIME = 15000L // 15 sec
    }
}

fun Dialog?.expand() {
    this?.let { dialog ->
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheetInternal =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.peekHeight = it.height
                it.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}