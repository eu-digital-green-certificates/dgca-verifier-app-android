/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 06/04/2022, 13:48
 */

package dgca.verifier.app.android.vc.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.android.app.vc.R
import com.android.app.vc.databinding.FragmentVcVerificationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VcVerificationFragment : BindingFragment<FragmentVcVerificationBinding>() {

    private val viewModel by viewModels<VcViewModel>()
    private val args by navArgs<VcVerificationFragmentArgs>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentVcVerificationBinding =
        FragmentVcVerificationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }

        viewModel.validate(args.qrCodeText)

        binding.close.setOnClickListener { requireActivity().finish() }
    }

    private fun onViewModelEvent(event: VcViewModel.ViewEvent) {
        when (event) {
            is VcViewModel.ViewEvent.OnError -> handleError(event.type)
            is VcViewModel.ViewEvent.OnVerified -> showVerified(event.subjectName, event.payloadInfo)
            is VcViewModel.ViewEvent.OnIssuerNotTrusted -> showConfirmationDialog(event.issuerDomain)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleError(error: VcViewModel.ErrorType) {
        val errorText = when (error) {
            VcViewModel.ErrorType.JWS_STRUCTURE_NOT_VALID -> "JWS_STRUCTURE_NOT_VALID"
            VcViewModel.ErrorType.KID_NOT_INCLUDED -> "KID_NOT_INCLUDED"
            VcViewModel.ErrorType.ISSUER_NOT_RECOGNIZED -> "ISSUER_NOT_RECOGNIZED"
            VcViewModel.ErrorType.ISSUER_NOT_INCLUDED -> "ISSUER_NOT_INCLUDED"
            VcViewModel.ErrorType.TIME_BEFORE_NBF -> "TIME_BEFORE_NBF"
            VcViewModel.ErrorType.VC_EXPIRED -> "VC_EXPIRED"
            VcViewModel.ErrorType.INVALID_SIGNATURE -> "INVALID_SIGNATURE"
        }

        binding.progressBar.isVisible = false
        binding.certStatusIcon.setImageResource(R.drawable.error)
        binding.verificationStatusBackground.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
        binding.status.text = "${getString(R.string.cert_invalid)}\n$errorText"
        binding.statusViews.isVisible = true
    }

    @SuppressLint("SetTextI18n")
    private fun showVerified(subjectName: VcViewModel.SubjectName, payloadInfo: String) {
        binding.progressBar.isVisible = false
        binding.certStatusIcon.setImageResource(R.drawable.check)
        binding.verificationStatusBackground.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
        binding.personFullName.text = "${subjectName.family} ${subjectName.given.firstOrNull() ?: ""}"
        binding.payloadInfo.text = payloadInfo
        binding.status.text = getString(R.string.cert_valid)
        binding.statusViews.isVisible = true
    }

    private fun showConfirmationDialog(issuerDomain: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.issuer_not_trusted, issuerDomain))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.approve)) { dialog, _ ->
                viewModel.issuerApproved()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            .show()
    }
}