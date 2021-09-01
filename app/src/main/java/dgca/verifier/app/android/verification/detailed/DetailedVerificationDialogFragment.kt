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
 *  Created by osarapulov on 8/31/21 9:27 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.DialogFragmentDetailedVerificationBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.verification.BaseVerificationDialogFragment
import dgca.verifier.app.android.verification.BaseVerificationViewModel
import dgca.verifier.app.android.verification.VerificationError

@AndroidEntryPoint
class DetailedVerificationDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentDetailedVerificationBinding>() {

    private val args by navArgs<DetailedVerificationDialogFragmentArgs>()
    private val viewModel by viewModels<DetailedBaseVerificationViewModel>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentDetailedVerificationBinding =
        DialogFragmentDetailedVerificationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailedVerificationResultHeaderView.setInfoClickListener {
            Toast.makeText(
                requireContext(),
                "TODO implement info click handler",
                Toast.LENGTH_SHORT
            ).show()
            // TODO implement handler
        }
        viewModel.detailedVerificationResult.observe(viewLifecycleOwner) {
            handleDetailedVerificationResult(it)
        }
    }

    private fun handleDetailedVerificationResult(detailedVerificationResult: DetailedVerificationResult) {
        binding.detailedVerificationResultHeaderView.setUp(
            detailedVerificationResult
        )

        val (colorRes, textRes) = detailedVerificationResult.verificationError.toVerificationComponentStates().toVerificationResult()
            .getActionButtonData()

        val context = requireContext()
        binding.actionButton.text = context.getString(textRes)
        binding.actionButton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        binding.actionButton.setOnClickListener { dismiss() }
        binding.dataLoadedViews.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        handleCertificateModel(detailedVerificationResult.certificateModel, detailedVerificationResult.verificationError)
    }

    private fun handleCertificateModel(certificateModel: CertificateModel?, verificationError: VerificationError?) {
        if (certificateModel == null) {
            binding.certificateInfo.visibility = View.GONE
        } else {
            binding.certificateInfo.setCertificateModel(certificateModel, verificationError)
            binding.certificateInfo.visibility = View.VISIBLE
        }
    }

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams

    override fun timerView(): View = binding.timberView

    override fun progressBar(): ProgressBar = binding.progressBar

    override fun qrCodeText(): String = args.qrCodeText

    override fun countryIsoCode(): String = args.countryIsoCode

    private fun VerificationResult.getActionButtonData(): Pair<Int, Int> = when (this) {
        VerificationResult.VALID -> Pair(R.color.green, R.string.done)
        VerificationResult.INVALID -> Pair(R.color.red, R.string.retry)
        VerificationResult.LIMITED_VALIDITY -> Pair(
            R.color.yellow,
            R.string.retry
        )
    }

    override fun viewModel(): BaseVerificationViewModel = viewModel
}