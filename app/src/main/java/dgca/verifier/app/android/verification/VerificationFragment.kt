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
 *  Created by osarapulov on 9/2/21 11:07 AM
 */

package dgca.verifier.app.android.verification

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.FragmentVerificationBinding
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer

@AndroidEntryPoint
class VerificationFragment : DialogFragment() {
    private val viewModel by viewModels<VerificationViewModel>()
    private val args by navArgs<VerificationFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init(args.qrCodeText, args.countryIsoCode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentVerificationBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.qrCodeVerificationResult.observe(viewLifecycleOwner) { qrCodeVerificationResult ->
            if (qrCodeVerificationResult is QrCodeVerificationResult.Applicable) {
                setFragmentResult(
                    VERIFY_REQUEST_KEY,
                    bundleOf(
                        STANDARDISED_VERIFICATION_RESULT_KEY to qrCodeVerificationResult.standardizedVerificationResult,
                        CERTIFICATE_MODEL_KEY to qrCodeVerificationResult.certificateModel,
                        HCERT_KEY to qrCodeVerificationResult.hcert,
                        RULE_VALIDATION_RESULT_MODELS_CONTAINER_KEY to qrCodeVerificationResult.rulesValidationResults?.let {
                            RuleValidationResultModelsContainer(it)
                        },
                        IS_DEBUG_MODE_ENABLED to qrCodeVerificationResult.isDebugModeEnabled,
                        DEBUG_DATA to qrCodeVerificationResult.debugData
                    )
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
    }
}