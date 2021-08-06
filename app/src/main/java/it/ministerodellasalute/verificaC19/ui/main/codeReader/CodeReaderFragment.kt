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

package it.ministerodellasalute.verificaC19.ui.main.codeReader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.databinding.FragmentCodeReaderBinding
import java.lang.Exception

class CodeReaderFragment : Fragment(), NavController.OnDestinationChangedListener,
    View.OnClickListener {

    private var _binding: FragmentCodeReaderBinding? = null
    private val binding get() = _binding!!

    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            // Prevent errors from finding patterns of other QR code types inside DCCs
            if (result.barcodeFormat != BarcodeFormat.QR_CODE && result.barcodeFormat != BarcodeFormat.AZTEC) {
                return
            }
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            binding.barcodeScanner.pause()

            lastText = result.text

            try {
                beepManager.playBeepSoundAndVibrate()
            } catch (e: Exception) {
            }

            navigateToVerificationPage(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback { requireActivity().finish() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.AZTEC, BarcodeFormat.QR_CODE)
        binding.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.initializeFromIntent(requireActivity().intent)
        binding.barcodeScanner.decodeContinuous(callback)
        binding.barcodeScanner.statusView.text = ""
        beepManager = BeepManager(requireActivity())

        binding.backImage.setOnClickListener(this)
        binding.backText.setOnClickListener(this)
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

    private fun navigateToVerificationPage(text: String) {
        findNavController().currentDestination

        val action = CodeReaderFragmentDirections.actionCodeReaderFragmentToVerificationFragment(
            text
        )
        findNavController().navigate(action)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back_image -> requireActivity().finish()
            R.id.back_text -> requireActivity().finish()
        }
    }
}