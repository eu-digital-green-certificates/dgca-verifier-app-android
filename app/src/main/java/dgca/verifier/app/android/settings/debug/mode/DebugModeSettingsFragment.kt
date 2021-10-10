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
 *  Created by osarapulov on 9/3/21 6:05 PM
 */

package dgca.verifier.app.android.settings.debug.mode

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.MainActivity
import dgca.verifier.app.android.R
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentDebugModeSettingsBinding
import dgca.verifier.app.android.utils.applyStyle

@AndroidEntryPoint
class DebugModeSettingsFragment : BindingFragment<FragmentDebugModeSettingsBinding>() {

    private val viewModel by viewModels<DebugModeSettingsViewModel>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDebugModeSettingsBinding =
        FragmentDebugModeSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)

        viewModel.debugModeState.observe(viewLifecycleOwner) {
            binding.debugModeSwitch.isChecked = it != DebugModeState.OFF
            when (it) {
                DebugModeState.LEVEL_1 -> binding.level1.isChecked = true
                DebugModeState.LEVEL_2 -> binding.level2.isChecked = true
                DebugModeState.LEVEL_3 -> binding.level3.isChecked = true
                else -> {
                }
            }
        }

        binding.debugModeSwitch.setOnCheckedChangeListener { _, _ -> saveSelectedDebugModeState() }
        binding.debugModeLevel.setOnCheckedChangeListener { _, _ -> saveSelectedDebugModeState() }

        setFragmentResultListener(COUNTRIES_SELECTOR_REQUEST_KEY) { _, bundle ->
            val countriesData: CountriesData =
                bundle.getParcelable(COUNTRIES_DATA_KEY)!!
            viewModel.saveSelectedCountries(countriesData)
        }

        viewModel.countriesData.observe(viewLifecycleOwner) { setUpSelectCountry(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSelectedDebugModeState() {
        getSelectedDebugMode().apply {
            viewModel.saveSelectedDebugMode(this)
        }
    }

    private fun showCountriesSelector(countriesData: CountriesData) {
        val action =
            DebugModeSettingsFragmentDirections.actionDebugModeSettingsFragmentToCountriesSelectorFragment(
                countriesData
            )
        findNavController().navigate(action)
    }

    private fun setUpSelectCountry(countriesData: CountriesData) {
        if (countriesData.availableCountriesCodes.isEmpty()) {
            return
        }

        val selectedCountriesText =
            if (countriesData.selectedCountriesCodes.isEmpty()) {
                getString(R.string.no_countries_selected)
            } else {
                countriesData.selectedCountriesCodes
                    .sorted()
                    .joinToString(separator = ", ")
            }

        val context = requireContext()
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.select_country).toSpannable().applyStyle(
                    context,
                    R.style.TextAppearance_Dgca_SettingsButtonHeader
                )
            )
            .append("\n")
            .append(
                selectedCountriesText.toSpannable().applyStyle(
                    context,
                    R.style.TextAppearance_Dgca_SettingsButtonSubHeader
                )
            )

        binding.selectedCountries.text = spannable
        binding.selectedCountries.setOnClickListener { showCountriesSelector(countriesData) }
        binding.selectedCountries.visibility = View.VISIBLE
    }

    private fun getSelectedDebugMode(): DebugModeState = when {
        !binding.debugModeSwitch.isChecked -> DebugModeState.OFF
        binding.debugModeLevel.checkedRadioButtonId == R.id.level1 -> DebugModeState.LEVEL_1
        binding.debugModeLevel.checkedRadioButtonId == R.id.level2 -> DebugModeState.LEVEL_2
        binding.debugModeLevel.checkedRadioButtonId == R.id.level3 -> DebugModeState.LEVEL_3
        else -> DebugModeState.OFF
    }
}