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
        setViewHeight()

        dialog.expand()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.actionBtn.setOnClickListener { dismiss() }

        viewModel.verificationResult.observe(viewLifecycleOwner, {
            setCertStatusUI(it.isValid())

            setCertDataVisibility(it.isValid())
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
            }
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressContainer.isVisible = it
        })

        viewModel.init(args.qrCodeText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setViewHeight() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val params = binding.content.layoutParams as FrameLayout.LayoutParams
        params.height = height - TOP_MARGIN.dpToPx()
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
                VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
            }
        )
    }

    private fun setCertDataVisibility(isValid: Boolean) {
        binding.errorDetails.visibility = if (isValid) View.GONE else View.VISIBLE
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
        binding.personFullName.text =
            getString(
                R.string.person_full_name_placeholder,
                certificate.person.givenName,
                certificate.person.familyName
            )
        binding.personStandardisedFamilyName.text = certificate.person.standardisedFamilyName
        binding.personStandardisedGivenName.text = certificate.person.standardisedGivenName
        binding.dateOfBirth.text =
            certificate.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
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

    companion object {
        private const val TOP_MARGIN = 50
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