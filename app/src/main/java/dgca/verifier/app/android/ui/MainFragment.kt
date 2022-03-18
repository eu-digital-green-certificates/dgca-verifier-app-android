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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentMainBinding

@AndroidEntryPoint
class MainFragment : BindingFragment<FragmentMainBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scanQrCode.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToQrReaderFragment())
        }
        binding.scanNfcCode.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToNfcFragment())
        }
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMainBinding =
        FragmentMainBinding.inflate(inflater, container, false)
}
