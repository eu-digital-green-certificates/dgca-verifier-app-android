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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dgca.verifier.app.android.databinding.FragmentCodeReaderBinding

class CodeReaderFragment : Fragment() {

    private var _binding: FragmentCodeReaderBinding? = null
    private val binding get() = _binding!!

    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text
            beepManager.playBeepSoundAndVibrate()

            val action =
                CodeReaderFragmentDirections.actionCodeReaderFragmentToResultFragment(result.text)
            findNavController().navigate(action)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
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
        beepManager = BeepManager(requireActivity())

        binding.resume.setOnClickListener { binding.barcodeScanner.resume() }
        binding.pause.setOnClickListener { binding.barcodeScanner.pause() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()
        lastText = ""
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }
}