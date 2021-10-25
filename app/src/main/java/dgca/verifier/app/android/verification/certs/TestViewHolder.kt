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
 *  Created by osarapulov on 6/18/21 8:58 AM
 */

package dgca.verifier.app.android.verification.certs

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemTestBinding
import dgca.verifier.app.android.model.TestModel
import dgca.verifier.app.android.utils.bindCountryWith
import dgca.verifier.app.android.utils.bindText
import dgca.verifier.app.android.utils.toFormattedDateTime

class TestViewHolder(private val binding: ItemTestBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: TestModel) {
        data.disease.value.bindText(binding.diseaseTitle, binding.diseaseValue)
        data.resultType.value.bindText(binding.testResultTitle, binding.testResultValue)
        data.dateTimeOfCollection.toFormattedDateTime().bindText(
            binding.dateOfCollectionTitle,
            binding.dateOfCollectionValue
        )
        data.typeOfTest.value.bindText(binding.typeOfTestTitle, binding.typeOfTestValue)
        data.countryOfVaccination.bindCountryWith(binding.countryTitle, binding.countryValue)
    }

    companion object {
        fun create(parent: ViewGroup) = TestViewHolder(ItemTestBinding.bind(parent))
    }
}