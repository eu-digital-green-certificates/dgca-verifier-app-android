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
 */

package it.ministerodellasalute.verificaC19.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.BuildConfig
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.databinding.ActivityFirstBinding
import it.ministerodellasalute.verificaC19.ui.main.MainActivity
import it.ministerodellasalute.verificaC19sdk.util.Utility
import it.ministerodellasalute.verificaC19sdk.model.FirstViewModel
import it.ministerodellasalute.verificaC19sdk.model.VerificationViewModel
import it.ministerodellasalute.verificaC19sdk.util.FORMATTED_DATE_LAST_SYNC
import it.ministerodellasalute.verificaC19sdk.util.TimeUtility.parseTo
import java.util.*

@AndroidEntryPoint
class FirstActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFirstBinding

    private val viewModel by viewModels<FirstViewModel>()

    private lateinit var sharedPreference: SharedPreferences

    private lateinit var verificationViewModel: VerificationViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openQrCodeReader()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.qrButton.setOnClickListener(this)
        binding.settings.setOnClickListener(this)
        binding.scanModeButton.setOnClickListener(this)

        val string = getString(R.string.version, BuildConfig.VERSION_NAME)
        val spannableString = SpannableString(string).also {
            it.setSpan(UnderlineSpan(), 0, it.length, 0)
            it.setSpan(StyleSpan(Typeface.BOLD), 0, it.length, 0)
        }
        binding.versionText.text = spannableString

        viewModel.getDateLastSync().let {
            binding.dateLastSyncText.text = getString(
                R.string.lastSyncDate,
                if (it == -1L) getString(R.string.notAvailable) else it.parseTo(
                    FORMATTED_DATE_LAST_SYNC
                )
            )
        }

        viewModel.fetchStatus.observe(this) {
            if (it) {
                binding.dateLastSyncText.text = getString(R.string.loading)
            } else {
                viewModel.getDateLastSync().let { date ->
                    binding.dateLastSyncText.text = getString(
                        R.string.lastSyncDate,
                        if (date == -1L) getString(R.string.notAvailable) else date.parseTo(
                            FORMATTED_DATE_LAST_SYNC
                        )
                    )
                }
            }
        }
        binding.privacyPolicyCard.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/pn.html"))
            startActivity(browserIntent)
        }
        binding.faqCard.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dgc.gov.it/web/faq.html"))
            startActivity(browserIntent)
        }

        sharedPreference = getSharedPreferences("dgca.verifier.app.pref", Context.MODE_PRIVATE)
        verificationViewModel = ViewModelProvider(this)[VerificationViewModel::class.java]

        verificationViewModel.scanMode.observe(this, {
            setScanModeTexts(it)
        })
    }

    private fun setScanModeTexts(currentScanMode: String) {
        if (!verificationViewModel.getScanModeFlag()) {
            val s = SpannableStringBuilder()
                .bold { append(getString(R.string.label_choose_scan_mode)) }
            binding.scanModeButton.text = s
        } else {
            var chosenScanMode =
                if (currentScanMode == "3G") getString(R.string.scan_mode_3G_header) else getString(
                    R.string.scan_mode_2G_header
                )
            chosenScanMode += "\n"
            val chosenScanModeText =
                if (currentScanMode == "3G") getString(R.string.scan_mode_3G) else getString(R.string.scan_mode_2G)
            val s = SpannableStringBuilder()
                .bold { append(chosenScanMode) }
                .append(chosenScanModeText)
            binding.scanModeButton.text = s
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            createPermissionAlert()
        } else {
            openQrCodeReader()
        }
    }

    private fun createPermissionAlert() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.privacyTitle))
            builder.setMessage(getString(R.string.privacy))
            builder.setPositiveButton(getString(R.string.next)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            builder.setNegativeButton(getString(R.string.back)) { _, _ ->
            }
            val dialog = builder.create()
            dialog.show()
        } catch (e: Exception) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    override fun onResume() {
        super.onResume()
        if (!sharedPreference.getBoolean("scan_mode_flag", false)) {
            val s = SpannableStringBuilder()
                .bold { append(getString(R.string.label_choose_scan_mode)) }
            binding.scanModeButton.text = s
        } else {
            var chosenScanMode =
                if (sharedPreference.getString("scan_mode", "3G") == "3G") getString(R.string.scan_mode_3G_header) else getString(R.string.scan_mode_2G_header)
            chosenScanMode += "\n"
            val chosenScanModeText =
                if (sharedPreference.getString("scan_mode", "3G") == "3G") getString(R.string.scan_mode_3G) else getString(R.string.scan_mode_2G)
            val s = SpannableStringBuilder()
                .bold { append(chosenScanMode) }
                .append(chosenScanModeText)
            binding.scanModeButton.text = s
        }
        viewModel.getAppMinVersion().let {
            if (Utility.versionCompare(it, BuildConfig.VERSION_NAME) > 0 || viewModel.isSDKVersionObsoleted()) {
                createForceUpdateDialog()
            }
        }
    }

    private fun openQrCodeReader() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.qrButton) {
            viewModel.getDateLastSync().let {
                if (it == -1L) {
                    createNoKeyAlert()
                    return
                } else if (!verificationViewModel.getScanModeFlag() && v.id != R.id.scan_mode_button) {
                    createNoScanModeChosenAlert()
                    return
                }
            }
        }
        when (v?.id) {
            R.id.qrButton -> checkCameraPermission()
            R.id.settings -> openSettings()
            R.id.scan_mode_button -> AlertDialogCaller.showScanModeChoiceAlertDialog(
                this,
                getString(R.string.label_scan_mode),
                arrayOf(getString(R.string.label_alert_dialog_option, getString(R.string.label_scan_mode), getString(R.string.scan_mode_2G_header).substringAfter(
                    ' '
                ).toUpperCase(Locale.ROOT)),
                    getString(R.string.label_alert_dialog_option, getString(R.string.label_scan_mode), getString(R.string.scan_mode_3G_header).substringAfter(' ').toUpperCase(
                        Locale.ROOT
                    )
                    )
                ),
                ViewModelProvider(this)[VerificationViewModel::class.java]
            )
        }
    }

    private fun createNoScanModeChosenAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.noKeyAlertTitle))
        builder.setMessage(getString(R.string.label_no_scan_mode_chosen))
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun createNoKeyAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.noKeyAlertTitle))
        builder.setMessage(getString(R.string.noKeyAlertMessage))
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun createForceUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.updateTitle))
        builder.setMessage(getString(R.string.updateMessage))

        builder.setPositiveButton(getString(R.string.updateLabel)) { _, _ ->
            openGooglePlay()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun openGooglePlay() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

}