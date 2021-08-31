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


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.DialogFragmentVerificationBinding


@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationDialogFragment :
    BaseVerificationDialogFragment<DialogFragmentVerificationBinding>() {

    private val args by navArgs<VerificationDialogFragmentArgs>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentVerificationBinding =
        DialogFragmentVerificationBinding.inflate(inflater, container, false)

    override fun contentLayout(): ViewGroup.LayoutParams =
        binding.content.layoutParams

    override fun timerView(): View = binding.timerView
    override fun rulesList(): RecyclerView = binding.rulesList
    override fun actionButton(): Button = binding.actionButton
    override fun progressBar(): ProgressBar = binding.progressBar

    override fun qrCodeText(): String = args.qrCodeText

    override fun countryIsoCode(): String = args.countryIsoCode
    override fun status(): TextView = binding.status
    override fun certStatusIcon(): ImageView = binding.certStatusIcon
    override fun verificationStatusBg(): View = binding.verificationStatusBg

    override fun reasonForCertificateInvalidityTitle(): TextView =
        binding.reasonForCertificateInvalidityTitle

    override fun reasonForCertificateInvalidityName(): TextView =
        binding.reasonForCertificateInvalidityName

    override fun greenCertificate(): ViewStub = binding.greenCertificate

    override fun reasonTestResultValue(): TextView = binding.reasonTestResultValue

    override fun certificateTypeText(): TextView = binding.certificateTypeText

    override fun personFullName(): TextView = binding.personFullName

    override fun personStandardisedGivenNameTitle(): TextView =
        binding.personStandardisedGivenNameTitle

    override fun personStandardisedFamilyName(): TextView = binding.personStandardisedFamilyName

    override fun personStandardisedGivenName(): TextView = binding.personStandardisedGivenName

    override fun dateOfBirthTitle(): TextView = binding.dateOfBirthTitle

    override fun dateOfBirth(): TextView = binding.dateOfBirth

    override fun generalInfo(): Group = binding.generalInfo

    override fun errorDetails(): Group = binding.errorDetails

    override fun successDetails(): Group = binding.successDetails

    override fun errorTestResult(): Group = binding.errorTestResult
}