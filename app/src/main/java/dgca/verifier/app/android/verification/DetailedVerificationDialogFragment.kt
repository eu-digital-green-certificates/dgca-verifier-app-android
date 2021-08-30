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
 *  Created by osarapulov on 8/30/21 10:02 AM
 */

package dgca.verifier.app.android.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.DialogFragmentDetailedVerificationBinding

@AndroidEntryPoint
class DetailedVerificationDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentDetailedVerificationBinding>() {

    private val args by navArgs<DetailedVerificationDialogFragmentArgs>()
    val viewModel by viewModels<DetailedVerificationViewModel>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentDetailedVerificationBinding =
        DialogFragmentDetailedVerificationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.verificationComponent.observe(viewLifecycleOwner) {

        }
    }

    override fun contentLayout(): ViewGroup.LayoutParams = binding.content.layoutParams

    override fun qrCodeText(): String = args.qrCodeText

    override fun countryIsoCode(): String = args.countryIsoCode

}