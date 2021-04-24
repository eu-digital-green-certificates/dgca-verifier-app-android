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
 *  Created by mykhailo.nester on 4/24/21 5:18 PM
 */

package dgca.verifier.app.android.verification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemVaccinationBinding
import dgca.verifier.app.decoder.chain.model.Vaccination

class VaccinationViewHolder(private val binding: ItemVaccinationBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            VaccinationViewHolder(ItemVaccinationBinding.inflate(inflater, parent, false))
    }

    fun bind(data: Vaccination) {
        binding.disease.text = "disease: ${data.disease}"
        binding.vaccine.text = "vaccine: ${data.vaccine}"
        binding.medicinalProduct.text = "medicinalProduct: ${data.medicinalProduct}"
        binding.authorizationHolder.text = "authorizationHolder: ${data.authorizationHolder}"
        binding.doseSequence.text = "doseSequence: ${data.doseSequence}"
        binding.doseTotalNumber.text = "doseTotalNumber: ${data.doseTotalNumber}"
        binding.lotNumber.text = "lotNumber: ${data.lotNumber}"
        binding.date.text = "date: ${data.date}"
        binding.administeringCentre.text = "administeringCentre: ${data.administeringCentre}"
        binding.country.text = "country: ${data.country}"
    }
}
