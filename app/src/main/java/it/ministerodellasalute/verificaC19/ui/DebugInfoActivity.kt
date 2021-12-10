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

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.R
import it.ministerodellasalute.verificaC19.databinding.ActivityDebugInfoBinding
import it.ministerodellasalute.verificaC19.ui.main.Extras
import it.ministerodellasalute.verificaC19sdk.model.DebugInfoWrapper
import it.ministerodellasalute.verificaC19sdk.model.FirstViewModel

@AndroidEntryPoint
class DebugInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebugInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val debugJson = intent.getStringExtra(Extras.DEBUG_INFO)
        val wrapper: DebugInfoWrapper = Gson().fromJson(debugJson, DebugInfoWrapper::class.java)
        setupWithWrapper(wrapper)
    }

    private fun setupWithWrapper(wrapper: DebugInfoWrapper) {
        binding.revokesValue.text =
            wrapper.revokesNumber?.toString() ?: "Revoche non scaricate"
        wrapper.kidList?.forEach {
            val textView = TextView(this).apply {
                textSize = 16f
                text = it
            }.also { it.setTextColor(ContextCompat.getColor(this, R.color.blue)) }
            binding.kidContainer.addView(textView)
        }
    }


}
