/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 3:02 PM
 */

package dgca.verifier.app.android.dcc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.app.dcc.databinding.FragmentDccIntermediateBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.dcc.settings.COUNTRY_SELECTED_DATA_KEY
import dgca.verifier.app.android.dcc.settings.SELECT_COUNTRY_REQUEST_KEY
import dgca.verifier.app.android.dcc.settings.debug.DccSelectCountryData

@AndroidEntryPoint
class IntermediateFragment : BindingFragment<FragmentDccIntermediateBinding>() {
    private val viewModel by viewModels<IntermediateViewModel>()
    private val args by navArgs<IntermediateFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(SELECT_COUNTRY_REQUEST_KEY) { _, bundle ->
            val selectCountryData: DccSelectCountryData =
                bundle.getParcelable(COUNTRY_SELECTED_DATA_KEY)!!
            viewModel.saveCountrySelected(selectCountryData)
        }
        viewModel.result.observe(viewLifecycleOwner) {
            when (it) {
                IntermediateResult.RetryResult -> binding.actionButton.visibility = View.VISIBLE
                is IntermediateResult.CountryNotSelectedResult -> {
                    val action =
                        IntermediateFragmentDirections.actionIntermediateFragmentToCountrySelectorFragment(
                            it.selectCountryData
                        )
                    findNavController().navigate(action)
                }
                is IntermediateResult.CountrySelectedResult -> {
                    val action =
                        IntermediateFragmentDirections.actionIntermediateFragmentToVerificationFragment(
                            args.qrCodeText,
                            it.selectedCountryIsoCode
                        )
                    findNavController().navigate(action)
                }
                IntermediateResult.ProgressResult -> {
                    binding.actionButton.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDccIntermediateBinding =
        FragmentDccIntermediateBinding.inflate(inflater, container, false)
}