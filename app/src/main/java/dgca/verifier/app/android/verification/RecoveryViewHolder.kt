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
import dgca.verifier.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.verifier.app.android.YEAR_MONTH_DAY
import dgca.verifier.app.android.databinding.ItemRecoveryBinding
import dgca.verifier.app.android.model.RecoveryModel
import dgca.verifier.app.android.parseFromTo

class RecoveryViewHolder(private val binding: ItemRecoveryBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: RecoveryModel) {
        binding.diseaseValue.text = data.disease
        binding.validFromValue.text = data.certificateValidFrom.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        binding.validUntilValue.text = data.certificateValidUntil.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        binding.dateOfPositiveValue.text = data.dateOfFirstPositiveTest.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        binding.countryValue.text = data.countryOfVaccination
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            RecoveryViewHolder(ItemRecoveryBinding.inflate(inflater, parent, false))
    }
}
