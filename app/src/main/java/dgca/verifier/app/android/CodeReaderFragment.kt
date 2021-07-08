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
 *  Created by Mykhailo Nester on 4/23/21 9:48 AM
 */

package dgca.verifier.app.android

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
import androidx.fragment.app.Fragment
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
import dgca.verifier.app.android.databinding.FragmentCodeReaderBinding
import java.util.*


private const val CAMERA_REQUEST_CODE = 1003

@AndroidEntryPoint
class CodeReaderFragment : Fragment(), NavController.OnDestinationChangedListener {

    private var _binding: FragmentCodeReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<CodeReaderViewModel>()

    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        setUpCountriesProcessing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        findNavController().addOnDestinationChangedListener(this)
        lastText = ""
    }

    override fun onPause() {
        super.onPause()
        findNavController().removeOnDestinationChangedListener(this)
        binding.barcodeScanner.pause()
    }

    private fun setUpCountriesProcessing() {
        viewModel.countries.observe(viewLifecycleOwner, { pair ->
            if (pair.first.isEmpty() || pair.second == null) {
                View.GONE
            } else {
                val countries = pair.first
                val refinedCountries = countries.map { COUNTRIES_MAP[it] ?: it }
                    .sortedBy { Locale("", it).displayCountry }
                binding.countrySelector.adapter = CountriesAdapter(refinedCountries, layoutInflater)
                if (pair.second!!.isNotBlank()) {
                    val selectedCountryIndex =
                        refinedCountries.indexOf(pair.second!!)
                    if (selectedCountryIndex >= 0) {
                        binding.countrySelector.setSelection(selectedCountryIndex)
                    }
                }
                binding.countrySelector.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.selectCountry(refinedCountries[position].toLowerCase(Locale.ROOT))
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                    }
                }
                View.VISIBLE
            }.apply {
                binding.validateWith.visibility = this
                binding.countrySelector.visibility = this
            }
        })
    }

    private fun navigateToVerificationPage(text: String) {
        val action =
            CodeReaderFragmentDirections.actionCodeReaderFragmentToVerificationFragment(
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

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id == R.id.codeReaderFragment) {
            binding.barcodeScanner.resume()
            lastText = ""
        }
    }

    companion object {
        private val COUNTRIES_MAP = mapOf("el" to "gr")
    }
}