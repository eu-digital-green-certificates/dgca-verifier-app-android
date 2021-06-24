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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.*
import dgca.verifier.app.android.databinding.DialogFragmentVerificationBinding
import dgca.verifier.app.android.model.CertificateData
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.TestResult

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationDialogFragment : BottomSheetDialogFragment() {

    private val args by navArgs<VerificationDialogFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

    private var _binding: DialogFragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CertListAdapter(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.actionBtn.setOnClickListener { dismiss() }

        viewModel.verificationResult.observe(viewLifecycleOwner, {
            if (it == null) {
                dismiss()
            } else {
                setCertStatusUI(it.isValid())
                setCertDataVisibility(it.isValid())
            }
        })
        viewModel.verificationError.observe(viewLifecycleOwner, {
            setCertStatusError(it)
        })
        viewModel.certificate.observe(viewLifecycleOwner, { certificate ->
            if (certificate != null) {
                toggleButton(certificate)
                showUserData(certificate)

                val list = getCertificateListData(certificate)
                adapter.update(list)

                startTimer()
            }
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressBar.isVisible = it
        })

        viewModel.init(args.qrCodeText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCertStatusUI(isValid: Boolean) {
        val text: String
        val imageId: Int
        val statusColor: ColorStateList
        val actionBtnText: String

        if (isValid) {
            text = getString(R.string.cert_valid)
            imageId = R.drawable.check
            statusColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
            actionBtnText = getString(R.string.done)
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
                VerificationError.CERTIFICATE_EXPIRED -> R.string.certificate_is_expired
                VerificationError.CERTIFICATE_REVOKED -> R.string.certificate_was_revoked
                VerificationError.VERIFICATION_FAILED -> R.string.verification_failed
                VerificationError.TEST_DATE_IS_IN_THE_FUTURE -> R.string.the_test_date_is_in_the_future
                VerificationError.TEST_RESULT_POSITIVE -> R.string.test_result_positive
                VerificationError.RECOVERY_NOT_VALID_SO_FAR -> R.string.recovery_not_valid_yet
                VerificationError.RECOVERY_NOT_VALID_ANYMORE -> R.string.recover_not_valid_anymore
                VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
            }
        )
        if (verificationError == VerificationError.TEST_RESULT_POSITIVE) {
            binding.errorTestResult.visibility = View.VISIBLE
            binding.reasonTestResultValue.text = TestResult.DETECTED.value
        } else {
            binding.errorTestResult.visibility = View.GONE
        }
    }

    private fun setCertDataVisibility(isValid: Boolean) {
        binding.errorDetails.visibility = if (isValid) View.GONE else View.VISIBLE
        if (isValid) {
            binding.errorTestResult.visibility = View.GONE
        }
        binding.nestedScrollView.visibility = if (isValid) View.VISIBLE else View.GONE
    }

    private fun getCertificateListData(certificate: CertificateModel): List<CertificateData> {
        val list = mutableListOf<CertificateData>()
        list.addAll(certificate.vaccinations ?: emptyList())
        list.addAll(certificate.tests ?: emptyList())
        list.addAll(certificate.recoveryStatements ?: emptyList())

        return list
    }

    private fun showUserData(certificate: CertificateModel) {
        binding.personFullName.text = certificate.getFullName()
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
        
        val dateOfBirth = certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
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
        when {
            certificate.vaccinations?.isNotEmpty() == true -> enableToggleBtn(binding.vacToggle)
            certificate.recoveryStatements?.isNotEmpty() == true -> enableToggleBtn(binding.recToggle)
            certificate.tests?.isNotEmpty() == true -> enableToggleBtn(binding.testToggle)
        }
    }

    private fun enableToggleBtn(button: MaterialButton) {
        button.toggle()
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun startTimer() {
        binding.timerView.animate()
            .setDuration(COLLAPSE_TIME)
            .translationX(0F)
            .withEndAction {
                if (isVisible) {
                    dismiss()
                }
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