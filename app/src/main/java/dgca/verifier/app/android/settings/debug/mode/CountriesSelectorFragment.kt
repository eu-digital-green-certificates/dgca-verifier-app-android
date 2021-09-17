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
 *  Created by osarapulov on 9/4/21 8:05 PM
 */

package dgca.verifier.app.android.settings.debug.mode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.MainActivity
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentCountriesSelectorBinding


@AndroidEntryPoint
class CountriesSelectorFragment : BindingFragment<FragmentCountriesSelectorBinding>() {
    private val args by navArgs<CountriesSelectorFragmentArgs>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCountriesSelectorBinding =
        FragmentCountriesSelectorBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)

        binding.countriesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CountriesAdapter(layoutInflater, args.countriesData)
        }

        binding.actionButton.setOnClickListener {
            close()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                close()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun close() {
        setFragmentResult(
            COUNTRIES_SELECTOR_REQUEST_KEY,
            bundleOf(
                COUNTRIES_DATA_KEY to (binding.countriesList.adapter as CountriesAdapter).getCountriesData()
            )
        )
        findNavController().popBackStack()
    }
}