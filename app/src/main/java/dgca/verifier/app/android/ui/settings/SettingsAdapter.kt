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
 *  Created by osarapulov on 3/17/22, 8:24 AM
 */

package dgca.verifier.app.android.ui.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemSettingsBinding

class SettingsAdapter(
    private val inflater: LayoutInflater,
    private val settings: List<Pair<String, Intent>>,
    private val onSettingsClick: (intent: Intent) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private fun create(inflater: LayoutInflater, parent: ViewGroup) =
        ViewHolder(ItemSettingsBinding.inflate(inflater, parent, false))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        create(inflater, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val setting = settings[position]
        holder.binding.title.text = setting.first
        holder.binding.title.setOnClickListener { onSettingsClick(setting.second) }
    }

    override fun getItemCount(): Int = settings.size

    inner class ViewHolder(val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root)
}
