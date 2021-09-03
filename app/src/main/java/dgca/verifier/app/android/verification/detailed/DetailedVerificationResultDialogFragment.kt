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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.DialogFragmentDetailedVerificationResultBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.verification.BaseVerificationDialogFragment
import dgca.verifier.app.android.verification.StandardizedVerificationResult
import dgca.verifier.app.android.verification.StandardizedVerificationResultCategory

@AndroidEntryPoint
class DetailedVerificationResultDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentDetailedVerificationResultBinding>() {

    private val args by navArgs<DetailedVerificationResultDialogFragmentArgs>()
    private val viewModel by viewModels<DetailedBaseVerificationResultViewModel>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentDetailedVerificationResultBinding =
        DialogFragmentDetailedVerificationResultBinding.inflate(inflater, container, false)

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
        handleDetailedVerificationResult(
            args.standardizedVerificationResult,
            args.certificateModel,
            args.hcert,
            args.ruleValidationResultModelsContainer
        )
        binding.shareBtn.setOnClickListener {
            viewModel.onShareClick(requireContext().cacheDir.path, args.certificateModel, args.hcert, args.debugData)
        }
    }

    private fun handleDetailedVerificationResult(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        hcert: String?,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?
    ) {
        binding.shareBtn.isVisible = true
        binding.detailedVerificationResultHeaderView.setUp(
            standardizedVerificationResult,
            certificateModel,
            ruleValidationResultModelsContainer
        )

        val (colorRes, textRes) = standardizedVerificationResult.category.getActionButtonData()

        val context = requireContext()
        binding.actionButton.text = context.getString(textRes)
        binding.actionButton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        binding.actionButton.setOnClickListener { dismiss() }

        handleCertificateModel(
            standardizedVerificationResult,
            certificateModel,
            hcert
        )

        viewModel.inProgress.observe(viewLifecycleOwner) {
            binding.shareProgressView.isVisible = it
        }
    }

    private fun handleCertificateModel(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        hcert: String?
    ) {
        if (certificateModel == null || hcert.isNullOrBlank()) {
            binding.certificateInfo.visibility = View.GONE
            binding.certificateRawInfo.visibility = View.GONE
        } else {
            binding.certificateInfo.setCertificateModel(
                certificateModel,
                standardizedVerificationResult
            )
            binding.certificateInfo.setExpanded(true)
            binding.certificateRawInfo.setHcert(hcert)
            binding.certificateInfo.visibility = View.VISIBLE
            binding.certificateRawInfo.visibility = View.VISIBLE
        }
    }

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams

    private fun StandardizedVerificationResultCategory.getActionButtonData(): Pair<Int, Int> =
        when (this) {
            StandardizedVerificationResultCategory.VALID -> Pair(R.color.green, R.string.done)
            StandardizedVerificationResultCategory.INVALID -> Pair(R.color.red, R.string.retry)
            StandardizedVerificationResultCategory.LIMITED_VALIDITY -> Pair(
                R.color.yellow,
                R.string.retry
            )
        }
}