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
 *  Created by mykhailo.nester on 10/10/2021, 09:14
 */

package dgca.verifier.app.android.reader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.MainActivity
import dgca.verifier.app.android.R
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentCodeReaderBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer
import dgca.verifier.app.android.verification.*
import dgca.verifier.app.android.verification.model.DebugData
import dgca.verifier.app.android.verification.model.StandardizedVerificationResult
import dgca.verifier.app.engine.data.source.countries.COUNTRIES_MAP
import timber.log.Timber
import java.util.*

private const val CAMERA_REQUEST_CODE = 1003

@AndroidEntryPoint
class CodeReaderFragment : BindingFragment<FragmentCodeReaderBinding>(), NavController.OnDestinationChangedListener {

    private val viewModel by viewModels<CodeReaderViewModel>()

    private var lastText: String? = null
    private var refinedCountries: List<String> = emptyList()

    private lateinit var beepManager: BeepManager
    private lateinit var adapter: CountriesAdapter

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            binding.barcodeScanner.pause()

            lastText = result.text
            beepManager.playBeepSoundAndVibrate()

            navigateToVerificationPage(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { requireActivity().finish() }
        (activity as MainActivity).clearBackground()
        adapter = CountriesAdapter(layoutInflater)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCodeReaderBinding =
        FragmentCodeReaderBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestCameraPermission()

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.AZTEC, BarcodeFormat.QR_CODE)
        binding.barcodeScanner.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.decodeContinuous(callback)
        beepManager = BeepManager(requireActivity())

        binding.settings.setOnClickListener {
            val action = CodeReaderFragmentDirections.actionCodeReaderFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

        setFragmentResultListener(VERIFY_REQUEST_KEY) { _, bundle ->
            val standardizedVerificationResult: StandardizedVerificationResult? =
                bundle.getSerializable(STANDARDISED_VERIFICATION_RESULT_KEY) as StandardizedVerificationResult?
            val certificateModel: CertificateModel? = bundle.getParcelable(CERTIFICATE_MODEL_KEY)
            val hcert: String? = bundle.getString(HCERT_KEY)
            val ruleValidationResultModelsContainer: RuleValidationResultModelsContainer? = bundle.getParcelable(
                RULE_VALIDATION_RESULT_MODELS_CONTAINER_KEY
            )
            val isDebugModeEnabled = bundle.getBoolean(IS_DEBUG_MODE_ENABLED)
            val debugData: DebugData? = bundle.getParcelable(DEBUG_DATA)

            if (standardizedVerificationResult != null) {
                showVerificationResult(
                    standardizedVerificationResult,
                    certificateModel,
                    hcert,
                    ruleValidationResultModelsContainer,
                    isDebugModeEnabled,
                    debugData
                )
            }
        }

        binding.countrySelector.adapter = adapter
        binding.countrySelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                viewModel.selectCountry(adapter.getItem(position))
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        viewModel.countries.observe(viewLifecycleOwner, { list ->
            val hasCountries = list.isNotEmpty()
            binding.validateWith.isVisible = hasCountries
            binding.countrySelector.isVisible = hasCountries

            refinedCountries = list.sortedBy { Locale("", COUNTRIES_MAP[it] ?: it).displayCountry }
            adapter.update(refinedCountries)
        })
        viewModel.selectedCountry.observe(viewLifecycleOwner) {
            val position = refinedCountries.indexOf(it)
            if (position > 0) {
                binding.countrySelector.setSelection(position)
            }
        }

        binding.nfcSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableNFC()
            } else {
                enableCamera()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findNavController().addOnDestinationChangedListener(this)
        lastText = ""

        if (binding.nfcSwitch.isChecked) {
            enableNFC()
        } else {
            enableCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        findNavController().removeOnDestinationChangedListener(this)
        binding.barcodeScanner.pause()
        (requireActivity() as MainActivity).disableNfcForegroundDispatch()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id == R.id.codeReaderFragment) {
            if (binding.nfcSwitch.isChecked) {
                enableNFC()
            } else {
                enableCamera()
            }

            lastText = ""
        }
    }

    private fun enableNFC() {
        binding.barcodeScanner.pause()
        binding.nfcOverlay.isVisible = true
        (requireActivity() as MainActivity).enableNfcForegroundDispatch()
    }

    private fun enableCamera() {
        binding.barcodeScanner.resume()
        binding.nfcOverlay.isVisible = false
        (requireActivity() as MainActivity).disableNfcForegroundDispatch()
    }

    private fun showVerificationResult(
        standardizedVerificationResult: StandardizedVerificationResult,
        certificateModel: CertificateModel?,
        hcert: String?,
        ruleValidationResultModelsContainer: RuleValidationResultModelsContainer?,
        isDebugModeEnabled: Boolean,
        debugData: DebugData?
    ) {
        findNavController().navigateUp()
        binding.barcodeScanner.pause()
        val action = if (isDebugModeEnabled) {
            CodeReaderFragmentDirections.actionCodeReaderFragmentToDetailedVerificationResultFragment(
                standardizedVerificationResult,
                certificateModel,
                hcert,
                ruleValidationResultModelsContainer,
                debugData
            )
        } else {
            CodeReaderFragmentDirections.actionCodeReaderFragmentToVerificationResultFragment(
                standardizedVerificationResult,
                certificateModel,
                ruleValidationResultModelsContainer
            )
        }
        findNavController().navigate(action)
    }

    private fun navigateToVerificationPage(text: String) {
        val action =
            CodeReaderFragmentDirections.actionCodeReaderFragmentToVerificationDialogFragment(
                text,
                binding.countrySelector.selectedItem?.toString() ?: ""
            )
        findNavController().navigate(action)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    fun onNdefMessageReceived(qrCodeText: String) {
        try {
            val action =
                CodeReaderFragmentDirections.actionCodeReaderFragmentToVerificationDialogFragment(
                qrCodeText,
                binding.countrySelector.selectedItem?.toString() ?: ""
                )
            findNavController().navigate(action)
        } catch (ex: Exception) {
            Timber.d("action_codeReaderFragment_to_verificationDialogFragment cannot be found from the current destination Destination.")
        }
    }
}
