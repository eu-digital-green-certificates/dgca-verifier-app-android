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

package dgca.verifier.app.android.inputrecognizer.intent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.FragmentIntentBinding
import dgca.verifier.app.android.inputrecognizer.InputRecognizerDataHandlerFragment

/**
 * To launch Intent fragment use command:
 * adb shell am start -n com.android.app.poc/dgca.verifier.app.android.MainActivity -a com.android.app.poc.INTENT -e "DATA_PARAM" DCC
 */
@AndroidEntryPoint
class IntentFragment : InputRecognizerDataHandlerFragment<FragmentIntentBinding>() {

    private val args: IntentFragmentArgs by navArgs()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentIntentBinding =
        FragmentIntentBinding.inflate(inflater, container, false)

    override fun toProtocolHandlerNavDirection(data: String) =
        IntentFragmentDirections.actionIntentFragmentToProtocolHandlerDialogFragment(data)

    override fun getData() = args.data
}
