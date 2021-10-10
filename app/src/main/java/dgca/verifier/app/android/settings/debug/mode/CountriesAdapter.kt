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
 *  Created by osarapulov on 9/4/21 9:41 PM
 */

package dgca.verifier.app.android.settings.debug.mode

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.ItemCountryBinding
import dgca.verifier.app.engine.data.source.countries.COUNTRIES_MAP
import java.util.*

class CountriesAdapter(private val inflater: LayoutInflater, countriesData: CountriesData) :
    RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

    private val availableCountriesCodes = countriesData.availableCountriesCodes.sortedBy {
        Locale(
            "",
            COUNTRIES_MAP[it] ?: it
        ).displayCountry
    }
    private val selectedCountriesCodes: MutableSet<String> = countriesData.selectedCountriesCodes.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = create(inflater, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val countryCode = availableCountriesCodes[position]
        holder.binding.root.text = Locale(
            "",
            COUNTRIES_MAP[countryCode] ?: countryCode
        ).displayCountry
        holder.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ResourcesCompat.getDrawable(
                holder.itemView.resources,
                if (selectedCountriesCodes.contains(countryCode)) R.drawable.ic_icon_checkmark_active else R.drawable.ic_icon_checkmark,
                null
            ),
            null,
            null,
            null
        )
        holder.binding.root.setOnClickListener {
            val isSelected = selectedCountriesCodes.contains(countryCode)
            if (isSelected) {
                selectedCountriesCodes.remove(countryCode)
            } else {
                selectedCountriesCodes.add(countryCode)
            }
            holder.binding.root.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ResourcesCompat.getDrawable(
                    holder.itemView.resources,
                    if (!isSelected) R.drawable.ic_icon_checkmark_active else R.drawable.ic_icon_checkmark,
                    null
                ),
                null,
                null,
                null
            )
        }
    }

    override fun getItemCount(): Int = availableCountriesCodes.size

    fun getCountriesData() = CountriesData(availableCountriesCodes.toSet(), selectedCountriesCodes)

    private fun create(inflater: LayoutInflater, parent: ViewGroup) =
        ViewHolder(
            ItemCountryBinding.inflate(
                inflater,
                parent,
                false
            )
        )

    inner class ViewHolder(val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root)
}