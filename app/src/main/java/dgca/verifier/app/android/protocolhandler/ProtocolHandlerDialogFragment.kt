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
 *  Created by osarapulov on 3/17/22, 8:25 AM
 */

package dgca.verifier.app.android.protocolhandler

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dgca.verifier.app.android.base.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.DialogFragmentProtocolHandlerBinding

@AndroidEntryPoint
class ProtocolHandlerDialogFragment : BindingDialogFragment<DialogFragmentProtocolHandlerBinding>() {

    private val args: ProtocolHandlerDialogFragmentArgs by navArgs()
    private val viewModel by viewModels<ProtocolHandlerViewModel>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentProtocolHandlerBinding =
        DialogFragmentProtocolHandlerBinding.inflate(inflater, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setStyle(STYLE_NO_FRAME, android.R.style.Theme)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.protocolHandlerResult.observe(viewLifecycleOwner) {
            when (it) {
                is ProtocolHandlerViewModel.ProtocolHandlerResult.NotApplicable -> setFragmentResult(
                    PROTOCOL_HANDLER_REQUEST_KEY,
                    bundleOf()
                )
                is ProtocolHandlerViewModel.ProtocolHandlerResult.Applicable -> setFragmentResult(
                    PROTOCOL_HANDLER_REQUEST_KEY,
                    bundleOf(PROTOCOL_HANDLER_RESULT_KEY to it.intent)
                )
            }
        }

        viewModel.init(args.data)
    }
}
