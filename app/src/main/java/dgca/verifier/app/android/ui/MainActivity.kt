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
 *  Created by osarapulov on 3/17/22, 8:23 AM
 */

package dgca.verifier.app.android.ui

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.inputrecognizer.navigateToSpecificModule
import dgca.verifier.app.android.inputrecognizer.nfc.NdefParser
import dgca.verifier.app.android.protocolhandler.ProtocolHandlerViewModel
import timber.log.Timber

const val INTENT_ACTION = "com.android.app.poc.INTENT"
const val DATA_PARAM_KEY = "DATA_PARAM"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private val viewModel by viewModels<MainViewModel>()
    private val protocolViewModel by viewModels<ProtocolHandlerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        handleIntent(intent)
        viewModel.init()

        protocolViewModel.protocolHandlerResult.observe(this) {
            if (it is ProtocolHandlerViewModel.ProtocolHandlerResult.Applicable) {
                navigateToSpecificModule(it.intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            INTENT_ACTION -> {
                val action = MainFragmentDirections.actionMainFragmentToIntentFragment(
                    intent.getStringExtra(
                        DATA_PARAM_KEY
                    )!!
                )
                navController.navigate(action)
            }
            NfcAdapter.ACTION_NDEF_DISCOVERED -> checkNdefMessage(intent)
        }
    }

    private fun checkNdefMessage(intent: Intent) {
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
            val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
            parseNdefMessages(messages)
            intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }
    }

    private fun parseNdefMessages(messages: List<NdefMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val builder = StringBuilder()
        val records = NdefParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str)
        }

        val qrCodeText = builder.toString()
        if (qrCodeText.isNotEmpty()) {
            protocolViewModel.init(qrCodeText)
        } else {
            Timber.d("Received empty NDEFMessage")
        }
    }
}
