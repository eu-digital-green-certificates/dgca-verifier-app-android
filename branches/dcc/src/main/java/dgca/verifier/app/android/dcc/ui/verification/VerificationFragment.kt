/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 2:03 PM
 */

package dgca.verifier.app.android.dcc.ui.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.app.dcc.databinding.FragmentVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.dcc.model.CertificateModel
import dgca.verifier.app.android.dcc.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.dcc.ui.BindingFragment
import dgca.verifier.app.android.dcc.ui.verification.model.DebugData
import dgca.verifier.app.android.dcc.ui.verification.model.QrCodeVerificationResult
import dgca.verifier.app.android.dcc.ui.verification.model.StandardizedVerificationResult

@AndroidEntryPoint
class VerificationFragment : BindingFragment<FragmentVerificationBinding>() {

    private val viewModel by viewModels<VerificationViewModel>()
    private val args by navArgs<VerificationFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(args.qrCodeText, args.countryIsoCode)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerificationBinding =
        FragmentVerificationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.qrCodeVerificationResult.observe(viewLifecycleOwner) { qrCodeVerificationResult ->
            if (qrCodeVerificationResult is QrCodeVerificationResult.Applicable) {
                val standardizedVerificationResult =
                    qrCodeVerificationResult.standardizedVerificationResult
                val certificateModel = qrCodeVerificationResult.certificateModel
                val hcert = qrCodeVerificationResult.hcert
                val ruleValidationResultModelsContainer =
                    qrCodeVerificationResult.rulesValidationResults?.let {
                        RuleValidationResultModelsContainer(it)
                    }
                val isDebugModeEnabled = qrCodeVerificationResult.isDebugModeEnabled
                val debugData = qrCodeVerificationResult.debugData

                showVerificationResult(
                    standardizedVerificationResult,
                    certificateModel,
                    hcert,
                    ruleValidationResultModelsContainer,
                    isDebugModeEnabled,
                    debugData
                )
            } else {
                Toast.makeText(requireContext(), "Not applicable", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }

    private fun showVerificationResult(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        hcert: String?,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?,
        isDebugModeEnabled: Boolean,
        debugData: DebugData?
    ) {
        val action = if (isDebugModeEnabled) {
            VerificationFragmentDirections.actionCodeReaderFragmentToDetailedVerificationResultFragment(
                standardizedVerificationResult,
                certificateModel,
                hcert,
                ruleValidationResultModelsContainer,
                debugData
            )
        } else {
            VerificationFragmentDirections.actionCodeReaderFragmentToVerificationResultFragment(
                standardizedVerificationResult,
                certificateModel,
                ruleValidationResultModelsContainer
            )
        }
        findNavController().navigate(action)
    }
}
