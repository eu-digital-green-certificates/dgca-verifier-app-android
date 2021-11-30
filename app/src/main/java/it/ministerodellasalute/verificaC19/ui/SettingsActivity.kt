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
 *  Created by nicolamcornelio on 07/10/2021, 11:49
 */

package it.ministerodellasalute.verificaC19.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.databinding.ActivitySettingsBinding
import it.ministerodellasalute.verificaC19sdk.model.VerificationViewModel

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel by viewModels<VerificationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSwitchesValue()
        setTextViewsValue()
        setLiveDataObservers()
        setButtonsListener()
    }

    private fun setButtonsListener() {
        binding.backImage.setOnClickListener(this)
        binding.backText.setOnClickListener(this)
        binding.totemSwitch.setOnClickListener(this)
        binding.scanCard.setOnClickListener(this)
        binding.faqCard.setOnClickListener(this)
        binding.privacyPolicyCard.setOnClickListener(this)
    }

    private fun setSwitchesValue() {
        binding.totemSwitch.isChecked = viewModel.getTotemMode()
    }

    private fun setTextViewsValue() {
        val chosenScanMode =
            if (viewModel.getScanMode() == "3G") getString(R.string.scan_mode_3G_short) else getString(
                R.string.scan_mode_2G_short
            )
        binding.chosenScanText.text = chosenScanMode
    }

    private fun setLiveDataObservers() {
        viewModel.scanMode.observe(this, {
            val chosenScanMode =
                if (it == "3G") getString(R.string.scan_mode_3G_short) else getString(R.string.scan_mode_2G_short)
            binding.chosenScanText.text = chosenScanMode
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.back_image || v?.id == R.id.back_text) {
            finish()
        } else if (v?.id == R.id.totem_switch) {
            viewModel.setTotemMode(binding.totemSwitch.isChecked)
        } else if (v?.id == R.id.scan_card) {
            AlertDialogCaller.showScanModeChoiceAlertDialog(
                this,
                getString(R.string.label_scan_mode),
                arrayOf(
                    getString(R.string.scan_mode_2G),
                    getString(R.string.scan_mode_3G)
                ),
                viewModel
            )
        } else if (v?.id == R.id.faq_card) {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/pn.html"))
            startActivity(browserIntent)
        } else if (v?.id == R.id.privacy_policy_card) {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/faq.html"))
            startActivity(browserIntent)
        }
    }
}