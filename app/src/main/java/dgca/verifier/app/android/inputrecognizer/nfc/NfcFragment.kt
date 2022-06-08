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
 *  Created by osarapulov on 3/17/22, 8:28 AM
 */

package dgca.verifier.app.android.inputrecognizer.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentNfcBinding
import dgca.verifier.app.android.ui.MainActivity
import timber.log.Timber

/**
 * To launch NFC fragment use command:
 * adb shell am start -n com.android.app.poc/dgca.verifier.app.android.MainActivity -a android.nfc.action.NDEF_DISCOVERED
 */
@AndroidEntryPoint
class NfcFragment : BindingFragment<FragmentNfcBinding>() {

    private var adapter: NfcAdapter? = null

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNfcBinding =
        FragmentNfcBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        if (adapter == null) {
            val nfcManager = requireActivity().getSystemService(Context.NFC_SERVICE) as NfcManager
            adapter = nfcManager.defaultAdapter
        }

        try {
            val intent = Intent(requireActivity(), MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(requireActivity(), 0, intent, 0)
            adapter?.enableForegroundDispatch(requireActivity(), nfcPendingIntent, null, null)
            binding.stateTextView.text = getString(R.string.nfc_enabled)
            Timber.d("NFC enabled")

        } catch (ex: IllegalStateException) {
            binding.stateTextView.text = getString(R.string.nfc_enable_error)
            Timber.e(ex, "Error enabling NFC foreground dispatch")
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(requireActivity())
            Timber.d("NFC disabled")

        } catch (ex: IllegalStateException) {
            Timber.e(ex, "Error disabling NFC foreground dispatch")
        }
    }
}
