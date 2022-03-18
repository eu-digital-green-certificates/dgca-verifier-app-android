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
 *  Created by osarapulov on 3/17/22, 8:27 AM
 */

package dgca.verifier.app.android.inputrecognizer.qrreader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.FragmentQrReaderBinding
import dgca.verifier.app.android.inputrecognizer.InputRecognizerFragment

private const val CAMERA_REQUEST_CODE = 1003

@AndroidEntryPoint
class QrReaderFragment : InputRecognizerFragment<FragmentQrReaderBinding>() {

    private var lastText: String? = null
    private lateinit var beepManager: BeepManager

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }
            binding.barcodeScanner.pause()

            lastText = result.text
            beepManager.playBeepSoundAndVibrate()

            navigateToProtocolHandler(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQrReaderBinding = FragmentQrReaderBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraPermission()

        val formats: Collection<BarcodeFormat> = listOf(BarcodeFormat.AZTEC, BarcodeFormat.QR_CODE)
        binding.barcodeScanner.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.decodeContinuous(callback)
        beepManager = BeepManager(requireActivity())

        binding.settings.setOnClickListener {
            val action = QrReaderFragmentDirections.actionQrReaderFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    override fun onPause() {
        binding.barcodeScanner.pause()
        super.onPause()
    }

    override fun toProtocolHandlerNavDirection(data: String) =
        QrReaderFragmentDirections.actionQrReaderFragmentToProtocolHandlerDialogFragment(data)

    override fun handleError() {
        reset()
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

    private fun reset() {
        lastText = ""
        binding.barcodeScanner.resume()
    }
}
