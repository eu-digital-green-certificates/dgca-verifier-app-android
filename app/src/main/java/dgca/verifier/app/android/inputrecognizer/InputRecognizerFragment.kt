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
 *  Created by osarapulov on 3/17/22, 8:26 AM
 */

package dgca.verifier.app.android.inputrecognizer

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.protocolhandler.PROTOCOL_HANDLER_REQUEST_KEY
import dgca.verifier.app.android.protocolhandler.PROTOCOL_HANDLER_RESULT_KEY

abstract class InputRecognizerFragment<T : ViewBinding> : BindingFragment<T>() {
    abstract fun toProtocolHandlerNavDirection(data: String): NavDirections
    abstract fun handleError()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setFragmentResultListener(PROTOCOL_HANDLER_REQUEST_KEY) { _, bundle ->
            findNavController().navigateUp()
            val intent: Intent? = bundle.getParcelable(PROTOCOL_HANDLER_RESULT_KEY)
            if (intent != null) {
                requireActivity().navigateToSpecificModule(intent) { handleError() }
            } else {
                handleError()
            }
        }
    }

    fun navigateToProtocolHandler(data: String) {
        val action = toProtocolHandlerNavDirection(data)
        findNavController().navigate(action)
    }
}

fun Activity.navigateToSpecificModule(intent: Intent, error: (() -> Unit)? = null) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show()
        error?.invoke()
    }
}
