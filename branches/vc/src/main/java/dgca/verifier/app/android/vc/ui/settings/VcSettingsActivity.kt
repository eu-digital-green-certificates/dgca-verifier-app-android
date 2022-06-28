/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 27/04/2022, 00:18
 */

package dgca.verifier.app.android.vc.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.app.vc.R
import com.android.app.vc.databinding.ActivityVcSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.vc.formatWith
import dgca.verifier.app.android.vc.toLocalDateTime

@AndroidEntryPoint
class VcSettingsActivity : AppCompatActivity() {

    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var binding: ActivityVcSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.syncTrustedList.setOnClickListener { viewModel.reloadTrustList() }
        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.lastTimeSync.observe(this) {
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
        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
    }

    private fun onViewModelEvent(event: SettingsViewModel.ViewEvent) {
        when (event) {
            is SettingsViewModel.ViewEvent.OnError -> Toast.makeText(this, "Error: ${event.error}", Toast.LENGTH_SHORT).show()
            is SettingsViewModel.ViewEvent.OnLoading -> binding.progressBar.isVisible = event.isLoading
        }
    }

    companion object {
        private const val LAST_UPDATE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    }
}
