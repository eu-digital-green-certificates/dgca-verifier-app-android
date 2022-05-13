/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 2:56 PM
 */

package dgca.verifier.app.android.diia.ui.verification.certs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.app.diia.databinding.ItemRecoveryBinding
import dgca.verifier.app.android.diia.model.RecoveryModel
import dgca.verifier.app.android.diia.utils.*

class RecoveryViewHolder(private val binding: ItemRecoveryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(data: RecoveryModel) {
        data.disease.value.bindText(binding.diseaseTitle, binding.diseaseValue)
        val validFrom =
            data.certificateValidFrom.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        val validTo =
            data.certificateValidUntil.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)

        val validFromTo = if (validFrom.isNotBlank() && validTo.isNotBlank()) {
            "$validFrom - $validTo"
        } else {
            ""
        }

        validFromTo.bindText(binding.validFromTitle, binding.validFromValue)
        data.countryOfVaccination.bindCountryWith(binding.countryTitle, binding.countryValue)
    }

    companion object {
        fun create(parent: ViewGroup) = RecoveryViewHolder(ItemRecoveryBinding.bind(parent))
    }
}
