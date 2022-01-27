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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.nfc.NdefParser
import dgca.verifier.app.android.reader.CodeReaderFragment
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var adapter: NfcAdapter? = null
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.codeReaderFragment) {
                checkNdefMessage(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkNdefMessage(intent)
    }

    fun clearBackground() {
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
    }

    fun enableNfcForegroundDispatch() {
        if (adapter == null) {
            val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
            adapter = nfcManager.defaultAdapter
        }

        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)

        } catch (ex: IllegalStateException) {
            Timber.e(ex, "Error enabling NFC foreground dispatch")
        }
    }


    fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Timber.e(ex, "Error disabling NFC foreground dispatch")
        }
    }

    private fun checkNdefMessage(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                parseNdefMessages(messages)
                intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }
        }
    }

    private fun parseNdefMessages(messages: List<NdefMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val itr = messages.listIterator()

        while (itr.hasNext()) {
            val records = NdefParser.parse(itr.next())

            for (i in 0 until records.size) {
                if (records[i] != null) {
                    val record = records[i].str()

                    if (record.length >= 5 && record.substring(0, 4) == "HC1:") {
                       navHostFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                            if (fragment is CodeReaderFragment && fragment.isVisible) {
                                fragment.onNdefMessageReceived(record)
                            }
                        }
                    }
                }
            }
        }
    }
}