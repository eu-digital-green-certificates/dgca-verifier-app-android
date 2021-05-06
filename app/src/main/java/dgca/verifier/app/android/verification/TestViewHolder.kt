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
import dgca.verifier.app.android.DATE_TIME
import dgca.verifier.app.android.FORMATTED_DATE_TIME
import dgca.verifier.app.android.databinding.ItemTestBinding
import dgca.verifier.app.android.model.TestModel
import dgca.verifier.app.android.parseFromTo

class TestViewHolder(private val binding: ItemTestBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            TestViewHolder(ItemTestBinding.inflate(inflater, parent, false))
    }

    fun bind(data: TestModel) {
        binding.testResultValue.text = data.testResult
        binding.dateOfCollectionValue.text = data.dateTimeOfCollection.parseFromTo(DATE_TIME, FORMATTED_DATE_TIME)
        binding.dateOfTestResultValue.text = data.dateTimeOfTestResult?.parseFromTo(DATE_TIME, FORMATTED_DATE_TIME)
        binding.diseaseValue.text = data.disease
        binding.typeOfTestValue.text = data.typeOfTest
        binding.countryValue.text = data.countryOfVaccination
    }
}
