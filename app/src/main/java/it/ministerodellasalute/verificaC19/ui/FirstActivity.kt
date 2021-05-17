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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.FORMATTED_DATE_LAST_SYNC
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.databinding.ActivityFirstBinding
import it.ministerodellasalute.verificaC19.parseTo
import it.ministerodellasalute.verificaC19.ui.main.MainActivity

@AndroidEntryPoint
class FirstActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFirstBinding

    private val viewModel by viewModels<FirstViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openQrCodeReader()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.qrButton.setOnClickListener(this)

        viewModel.getDateLastSync().let{
            binding.dateLastSyncText.text = getString(R.string.lastSyncDate, it.parseTo(FORMATTED_DATE_LAST_SYNC))
        }

        viewModel.fetchStatus.observe(this){
            if(it){
                binding.qrButton.isEnabled = false
                binding.dateLastSyncText.text = getString(R.string.lastSyncDate, getString(R.string.loading))
            }else{
                binding.qrButton.isEnabled = true
                viewModel.getDateLastSync().let{ date ->
                    binding.dateLastSyncText.text = getString(R.string.lastSyncDate, date.parseTo(FORMATTED_DATE_LAST_SYNC))
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }else{
            openQrCodeReader()
        }
    }

    private fun openQrCodeReader(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.qrButton -> checkCameraPermission()
        }
    }
}