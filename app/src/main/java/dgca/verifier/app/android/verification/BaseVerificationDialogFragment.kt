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
 *  Created by osarapulov on 8/30/21 8:58 AM
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
import android.view.ViewStub
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dgca.verifier.app.android.*
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.TestResult
import dgca.verifier.app.android.verification.certs.RecoveryViewHolder
import dgca.verifier.app.android.verification.certs.TestViewHolder
import dgca.verifier.app.android.verification.certs.VaccinationViewHolder
import dgca.verifier.app.android.verification.rules.RuleValidationResultCard
import dgca.verifier.app.android.verification.rules.RuleValidationResultsAdapter
import dgca.verifier.app.android.verification.rules.toRuleValidationResultCard

abstract class BaseVerificationDialogFragment<T : ViewBinding> : BottomSheetDialogFragment() {
    private val viewModel by viewModels<VerificationViewModel>()
    private val hideLiveData: MutableLiveData<Void?> = MutableLiveData()

    abstract fun contentLayout(): ViewGroup.LayoutParams
    open fun timerView(): View? = null
    open fun rulesList(): RecyclerView? = null
    open fun actionButton(): Button? = null
    open fun progressBar(): ProgressBar? = null
    abstract fun qrCodeText(): String
    abstract fun countryIsoCode(): String
    open fun status(): TextView? = null
    open fun certStatusIcon(): ImageView? = null
    open fun verificationStatusBg(): View? = null

    open fun reasonForCertificateInvalidityTitle(): TextView? = null
    open fun reasonForCertificateInvalidityName(): TextView? = null

    open fun greenCertificate(): ViewStub? = null
    open fun reasonTestResultValue(): TextView? = null
    open fun certificateTypeText(): TextView? = null

    open fun personFullName(): TextView? = null
    open fun personStandardisedGivenNameTitle(): TextView? = null
    open fun personStandardisedFamilyName(): TextView? = null
    open fun personStandardisedGivenName(): TextView? = null
    open fun dateOfBirthTitle(): TextView? = null
    open fun dateOfBirth(): TextView? = null

    open fun generalInfo(): Group? = null
    open fun errorDetails(): Group? = null
    open fun successDetails(): Group? = null
    open fun errorTestResult(): Group? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        contentLayout().apply {
            height = displayMetrics.heightPixels - TOP_MARGIN.dpToPx()
        }

        timerView()?.apply {
            translationX = -displayMetrics.widthPixels.toFloat()
        }


        dialog.expand()

        hideLiveData.observe(viewLifecycleOwner, {
            dismiss()
        })

        rulesList()?.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        actionButton()?.setOnClickListener { dismiss() }

        viewModel.verificationData.observe(viewLifecycleOwner, { verificationData ->
            if (verificationData.verificationResult == null) {
                hideLiveData.value = null
            } else {
                handleVerificationResult(verificationData)
            }
        })
        viewModel.verificationError.observe(viewLifecycleOwner, { setCertStatusError(it) })
        viewModel.inProgress.observe(viewLifecycleOwner, { progressBar()?.isVisible = it })

        viewModel.init(qrCodeText(), countryIsoCode())
    }

    private fun handleVerificationResult(verificationData: VerificationData) {
        setCertStatusUI(verificationData.getGeneralResult())
        setCertDataVisibility(verificationData.getGeneralResult())
        verificationData.certificateModel?.let { certificateModel ->
            personFullName()?.text = certificateModel.getFullName()
            toggleButton(certificateModel)

            if (verificationData.getGeneralResult() != GeneralVerificationResult.FAILED) {
                showUserData(certificateModel)

                if (greenCertificate()?.parent != null) {
                    when {
                        certificateModel.vaccinations?.size == 1 -> {
                            greenCertificate()?.layoutResource =
                                R.layout.item_vaccination
                            greenCertificate()?.setOnInflateListener { stub, inflated ->
                                VaccinationViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.vaccinations.first())
                            }
                            greenCertificate()?.inflate()
                        }
                        certificateModel.recoveryStatements?.size == 1 -> {
                            greenCertificate()?.layoutResource = R.layout.item_recovery

                            greenCertificate()?.setOnInflateListener { stub, inflated ->
                                RecoveryViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.recoveryStatements.first())
                            }
                            greenCertificate()?.inflate()
                        }
                        certificateModel.tests?.size == 1 -> {
                            greenCertificate()?.layoutResource = R.layout.item_test

                            greenCertificate()?.setOnInflateListener { stub, inflated ->
                                TestViewHolder.create(
                                    inflated as ViewGroup
                                ).bind(certificateModel.tests.first())
                            }
                            greenCertificate()?.inflate()
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

        status()?.text = text
        certStatusIcon()?.setImageResource(imageId)
        verificationStatusBg()?.backgroundTintList = statusColor
        actionButton()?.isVisible = true
        actionButton()?.backgroundTintList = statusColor
        actionButton()?.text = actionBtnText
        actionButton()?.isVisible = true
    }

    private fun setCertStatusError(verificationError: VerificationError) {
        reasonForCertificateInvalidityTitle()?.visibility = View.VISIBLE
        reasonForCertificateInvalidityName()?.visibility = View.VISIBLE
        reasonForCertificateInvalidityName()?.text = getString(
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
            errorTestResult()?.visibility = View.VISIBLE
            reasonTestResultValue()?.text = TestResult.DETECTED.value
        } else {
            errorTestResult()?.visibility = View.GONE
        }

        if (verificationError == VerificationError.RULES_VALIDATION_FAILED) {
            val ruleValidationResultCards = mutableListOf<RuleValidationResultCard>()
            val context = requireContext()
            viewModel.validationResults.value?.forEach {
                ruleValidationResultCards.add(
                    it.toRuleValidationResultCard(context)
                )

            }
            rulesList()?.adapter =
                RuleValidationResultsAdapter(layoutInflater, ruleValidationResultCards)
            reasonForCertificateInvalidityName()?.setOnClickListener {
                rulesList()?.visibility =
                    if (rulesList()?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                reasonForCertificateInvalidityName()?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(
                        resources,
                        if (rulesList()?.visibility == View.VISIBLE) R.drawable.icon_collapsed else R.drawable.icon_expanded,
                        null
                    ),
                    null
                )
            }
            reasonForCertificateInvalidityTitle()?.text =
                getString(R.string.possible_limitation)
            val outValue = TypedValue()
            requireContext().theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
            )
            reasonForCertificateInvalidityName()?.setBackgroundResource(outValue.resourceId)
            reasonForCertificateInvalidityName()?.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ResourcesCompat.getDrawable(resources, R.drawable.icon_expanded, null),
                null
            )
        }
    }

    private fun setCertDataVisibility(generalVerificationResult: GeneralVerificationResult) {
        errorDetails()?.visibility =
            if (generalVerificationResult == GeneralVerificationResult.SUCCESS) View.GONE else View.VISIBLE
        if (generalVerificationResult == GeneralVerificationResult.SUCCESS) {
            errorTestResult()?.visibility = View.GONE
        }
        successDetails()?.visibility =
            if (generalVerificationResult != GeneralVerificationResult.FAILED) View.VISIBLE else View.GONE
    }


    private fun showUserData(certificate: CertificateModel) {
        personStandardisedFamilyName()?.text = certificate.person.standardisedFamilyName
        personStandardisedGivenName()?.text = certificate.person.standardisedGivenName
        if (certificate.person.standardisedGivenName?.isNotBlank() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            personStandardisedGivenNameTitle()?.visibility = this
            personStandardisedGivenName()?.visibility = this
        }

        dateOfBirth()?.text =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)

        val dateOfBirth =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        if (dateOfBirth.isBlank()) {
            View.GONE
        } else {
            dateOfBirth()?.text = dateOfBirth
            View.VISIBLE
        }.apply {
            dateOfBirthTitle()?.visibility = this
            dateOfBirth()?.visibility = this
        }
    }

    private fun toggleButton(certificate: CertificateModel) {
        certificateTypeText()?.text = when {
            certificate.vaccinations?.isNotEmpty() == true -> getString(
                R.string.type_vaccination,
                certificate.vaccinations.first().doseNumber,
                certificate.vaccinations.first().totalSeriesOfDoses
            )
            certificate.recoveryStatements?.isNotEmpty() == true -> getString(R.string.type_recovered)
            certificate.tests?.isNotEmpty() == true -> getString(R.string.type_test)
            else -> getString(R.string.type_test)
        }
        generalInfo()?.visibility = View.VISIBLE
    }

    private fun startTimer() {
        timerView()?.animate()
            ?.setDuration(COLLAPSE_TIME)
            ?.translationX(0F)
            ?.withEndAction {
                hideLiveData.value = null
            }
            ?.start()
    }

    private var _binding: T? = null
    val binding get() = _binding!!

    abstract fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    open fun onDestroyBinding(binding: T) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val innerBinding = onCreateBinding(inflater, container)
        _binding = innerBinding
        return innerBinding.root
    }

    override fun onDestroyView() {
        val innerBinding = _binding
        if (innerBinding != null) {
            onDestroyBinding(innerBinding)
        }

        _binding = null

        super.onDestroyView()
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