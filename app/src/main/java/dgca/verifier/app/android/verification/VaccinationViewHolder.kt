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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemVaccinationBinding
import dgca.verifier.app.decoder.model.Vaccination

class VaccinationViewHolder(private val binding: ItemVaccinationBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            VaccinationViewHolder(ItemVaccinationBinding.inflate(inflater, parent, false))
    }

    @SuppressLint("SetTextI18n")
    fun bind(data: Vaccination) {
        binding.disease.text = "Disease or agent targeted: \n${data.disease}"
        binding.vaccine.text = "Vaccine or prophylaxis: \n${data.vaccine}"
        binding.medicinalProduct.text = "Vaccine medicinal product: \n${data.medicinalProduct}"
        binding.authorizationHolder.text = "Holder / Manufacturer: \n${data.manufacturer}"
        binding.doseSequence.text = "Dose Number: \n${data.doseNumber}"
        binding.doseTotalNumber.text = "Total Series of Doses: \n${data.totalSeriesOfDoses}"
        binding.date.text = "Date of Vaccination: \n${data.dateOfVaccination}"
        binding.country.text = "Country of Vaccination: \n${data.countryOfVaccination}"
        binding.certificateIssuer.text = "Issuer: \n${data.certificateIssuer}"
        binding.certIdentifier.text = "Unique Certificate Identifier: \n${data.certificateIdentifier}"
    }
}
