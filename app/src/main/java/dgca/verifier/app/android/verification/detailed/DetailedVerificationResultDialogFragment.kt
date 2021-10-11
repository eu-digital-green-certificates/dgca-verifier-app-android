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

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.DialogFragmentDetailedVerificationResultBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.verification.BaseVerificationDialogFragment
import dgca.verifier.app.android.verification.model.StandardizedVerificationResult
import dgca.verifier.app.android.verification.model.StandardizedVerificationResultCategory
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class DetailedVerificationResultDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentDetailedVerificationResultBinding>() {

    private val args by navArgs<DetailedVerificationResultDialogFragmentArgs>()
    private val viewModel by viewModels<DetailedBaseVerificationResultViewModel>()

    private lateinit var adapter: CertificateContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CertificateContentAdapter(layoutInflater)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentDetailedVerificationResultBinding =
        DialogFragmentDetailedVerificationResultBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleDetailedVerificationResult(
            args.standardizedVerificationResult,
            args.certificateModel,
            args.hcert,
            args.ruleValidationResultModelsContainer,
        )
        binding.shareBtn.setOnClickListener {
            viewModel.onShareClick(
                requireContext().cacheDir.path,
                args.certificateModel,
                args.hcert,
                args.debugData
            )
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
    }

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams

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
        ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)).apply {
            binding.actionButton.backgroundTintList = this
            binding.shareBtn.backgroundTintList = this
        }
        binding.actionButton.setOnClickListener { dismiss() }

        handleCertificateModel(
            standardizedVerificationResult,
            certificateModel,
            hcert,
            ruleValidationResultModelsContainer
        )

        viewModel.inProgress.observe(viewLifecycleOwner) {
            binding.shareProgressView.isVisible = it
        }
    }

    private fun handleCertificateModel(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        hcert: String?,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?
    ) {
        if (certificateModel == null || hcert.isNullOrBlank()) {
            binding.certificateInfo.visibility = View.GONE
            binding.certificateContent.visibility = View.GONE
            binding.certificateRawInfo.visibility = View.GONE
        } else {
            binding.certificateInfo.setCertificateModel(
                certificateModel,
                standardizedVerificationResult,
                ruleValidationResultModelsContainer
            )
            binding.certificateContent.setCertificateModel(certificateModel, adapter)
            binding.certificateRawInfo.setHcert(hcert)

            binding.certificateInfo.visibility = View.VISIBLE
            binding.certificateContent.visibility = View.VISIBLE
            binding.certificateRawInfo.visibility = View.VISIBLE
        }
    }

    private fun StandardizedVerificationResultCategory.getActionButtonData(): Pair<Int, Int> =
        when (this) {
            StandardizedVerificationResultCategory.VALID -> Pair(R.color.green, R.string.done)
            StandardizedVerificationResultCategory.INVALID -> Pair(R.color.red, R.string.retry)
            StandardizedVerificationResultCategory.LIMITED_VALIDITY -> Pair(
                R.color.yellow,
                R.string.retry
            )
        }

    private fun onViewModelEvent(event: DetailedViewEvent) {
        when (event) {
            is DetailedViewEvent.OnZipCreated -> {
                val path = event.filePath

                if (path.isEmpty()) {
                    Toast.makeText(requireContext(), "Failed to prepare file", Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    val uri: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().applicationContext.packageName + ".provider",
                        File(path)
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                }

                val pm = requireActivity().packageManager
                if (intent.resolveActivity(pm) != null) {
                    Intent.createChooser(intent, getString(R.string.share))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Failed to share file", Toast.LENGTH_SHORT)
                        .show()
                    Timber.d("Cannot shared file")
                }
            }
        }
    }
}