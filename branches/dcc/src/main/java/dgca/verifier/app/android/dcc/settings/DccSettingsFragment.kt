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
 *  Created by osarapulov on 3/17/22, 1:59 PM
 */

package dgca.verifier.app.android.dcc.settings

import android.content.Intent
import android.net.Uri
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
import com.android.app.dcc.BuildConfig
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentDccSettingsBinding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.dcc.settings.debug.DccSelectCountryData
import dgca.verifier.app.android.dcc.settings.debug.mode.DebugModeState
import dgca.verifier.app.android.dcc.ui.BindingFragment
import dgca.verifier.app.android.dcc.utils.applyStyle
import dgca.verifier.app.android.dcc.utils.formatWith
import dgca.verifier.app.android.dcc.utils.toLocalDateTime

@AndroidEntryPoint
class DccSettingsFragment : BindingFragment<FragmentDccSettingsBinding>() {

    private val viewModel by viewModels<DccSettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDccSettingsBinding =
        FragmentDccSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity() as DccSettingsActivity).setSupportActionBar(binding.toolbar)
        binding.privacyInformation.setOnClickListener { launchWebIntent() }
        binding.licenses.setOnClickListener { openLicenses() }
        binding.syncPublicKeys.setOnClickListener { viewModel.syncPublicKeys() }
        binding.version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        viewModel.inProgress.observe(viewLifecycleOwner) {
            binding.privacyInformation.isClickable = it != true
            binding.licenses.isClickable = it != true
            binding.syncPublicKeys.isClickable = it != true
            binding.progressBar.visibility = if (it == true) View.VISIBLE else View.GONE
        }
        viewModel.lastSyncLiveData.observe(viewLifecycleOwner) {
            if (it <= 0) {
                binding.lastUpdate.visibility = View.GONE
            } else {
                binding.lastUpdate.visibility = View.VISIBLE
                binding.lastUpdate.text = getString(
                    R.string.last_updated,
                    it.toLocalDateTime().formatWith(LAST_UPDATE_DATE_TIME_FORMAT)
                )
            }
        }
        viewModel.debugModeState.observe(viewLifecycleOwner) {
            setUpDebugModeButton(it)
        }
        binding.debugMode.setOnClickListener {
            val action =
                DccSettingsFragmentDirections.actionSettingsFragmentToVerificationResultFragment()
            findNavController().navigate(action)
        }

        viewModel.lastCountriesSyncLiveData.observe(viewLifecycleOwner) {
            setCountriesReloadState(it)
        }
        binding.reloadCountries.setOnClickListener { viewModel.syncCountries() }

        setFragmentResultListener(SELECT_COUNTRY_REQUEST_KEY) { _, bundle ->
            val selectCountryData: DccSelectCountryData =
                bundle.getParcelable(COUNTRY_SELECTED_DATA_KEY)!!
            viewModel.saveCountrySelected(selectCountryData)
        }

        viewModel.selectCountryData.observe(viewLifecycleOwner) { selectCountryData ->
            setSelectCountryButton(selectCountryData.selectedCountryIsoCode)

            binding.selectedCountry.setOnClickListener {
                val action =
                    DccSettingsFragmentDirections.actionSettingsFragmentToCountrySelectorFragment(
                        selectCountryData
                    )
                findNavController().navigate(action)
            }
        }

        binding.syncRevocation.setOnClickListener { viewModel.syncRevocation() }
        viewModel.lastRevocationSyncTime.observe(viewLifecycleOwner) {
            if (it <= 0) {
                binding.lastRevocationUpdate.visibility = View.GONE
            } else {
                binding.lastRevocationUpdate.visibility = View.VISIBLE
                binding.lastRevocationUpdate.text = getString(
                    R.string.last_updated,
                    it.toLocalDateTime().formatWith(LAST_UPDATE_DATE_TIME_FORMAT)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reset()
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

    private fun launchWebIntent() {
        val page = Uri.parse(PRIVACY_POLICY)
        val intent = Intent(Intent.ACTION_VIEW, page)

        if (intent.resolveActivity(requireContext().packageManager) == null) {
            return
        }
        requireContext().startActivity(intent)
    }

    private fun openLicenses() {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.licenses))
        requireContext().apply {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
    }

    private fun setUpDebugModeButton(debugModeState: DebugModeState) {
        val context = requireContext()
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.debug_mode).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonHeader)
            )
            .append("\n")
            .append(
                getString(debugModeState.stringRes).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonSubHeader)
            )

        binding.debugMode.text = spannable
        binding.debugMode.visibility = View.VISIBLE
    }

    private fun setCountriesReloadState(lastUpdate: Long) {
        val updateText = when {
            lastUpdate < 0 -> {
                getString(R.string.never)
            }
            lastUpdate == 0L -> {
                getString(R.string.failed)
            }
            else -> {
                lastUpdate.toLocalDateTime().formatWith(LAST_UPDATE_DATE_TIME_FORMAT)
            }
        }
        val lastUpdatedText = getString(R.string.last_updated, updateText)
        val context = requireContext()
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.reload_countries).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonHeader)
            )
            .append("\n")
            .append(
                lastUpdatedText.toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonSubHeader)
            )

        binding.reloadCountries.text = spannable
    }

    private fun setSelectCountryButton(selectedCountryIsoCode: String?) {
        val context = requireContext()
        val selectedCountry: String = selectedCountryIsoCode ?: getString(R.string.none)
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.select_country).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonHeader)
            )
            .append("\n")
            .append(
                getString(R.string.selected, selectedCountry).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonSubHeader)
            )

        binding.selectedCountry.text = spannable
    }

    companion object {
        private const val PRIVACY_POLICY =
            "https://op.europa.eu/en/web/about-us/legal-notices/eu-mobile-apps"
        private const val LAST_UPDATE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    }
}