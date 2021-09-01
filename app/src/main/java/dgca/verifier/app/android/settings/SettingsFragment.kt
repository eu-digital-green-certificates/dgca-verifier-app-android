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
 *  Created by osarapulov on 5/12/21 2:55 PM
 */

package dgca.verifier.app.android.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.*
import dgca.verifier.app.android.base.BindingFragment
import dgca.verifier.app.android.databinding.FragmentSettingsBinding

@AndroidEntryPoint
class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        binding.privacyInformation.setOnClickListener { launchWebIntent() }
        binding.licenses.setOnClickListener { openLicenses() }
        binding.syncPublicKeys.setOnClickListener { viewModel.syncPublicKeys() }
        binding.version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.privacyInformation.isClickable = it != true
            binding.licenses.isClickable = it != true
            binding.syncPublicKeys.isClickable = it != true
            binding.progressBar.visibility = if (it == true) View.VISIBLE else View.GONE
        })
        viewModel.lastSyncLiveData.observe(viewLifecycleOwner, {
            if (it <= 0) {
                binding.lastUpdate.visibility = View.GONE
            } else {
                binding.lastUpdate.visibility = View.VISIBLE
                binding.lastUpdate.text = getString(
                    R.string.last_updated,
                    it.toLocalDateTime().formatWith(LAST_UPDATE_DATE_TIME_FORMAT)
                )
            }
        })
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

    companion object {
        const val PRIVACY_POLICY = "https://op.europa.eu/en/web/about-us/legal-notices/eu-mobile-apps"
        private const val LAST_UPDATE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    }
}