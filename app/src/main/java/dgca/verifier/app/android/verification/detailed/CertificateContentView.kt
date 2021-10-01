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
 *  Created by mykhailo.nester on 30/09/2021, 20:04
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.ViewCertificateContentViewBinding
import dgca.verifier.app.android.model.CertificateModel

class CertificateContentView(context: Context, attrs: AttributeSet?) :
    MaterialCardView(context, attrs) {

    private val binding: ViewCertificateContentViewBinding =
        ViewCertificateContentViewBinding.inflate(LayoutInflater.from(context), this)

    private var isExpanded = false

    init {
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            context.resources.getDimension(R.dimen.detailed_verification_result_banner_radius),
            context.resources.displayMetrics
        )
        strokeWidth = resources.getDimensionPixelSize(R.dimen.default_stroke_width)
        setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)))

        binding.expandButton.setOnClickListener {
            setExpanded(!isExpanded)
        }

    }

    fun setCertificateModel(certificateModel: CertificateModel, certAdapter: CertificateContentAdapter) {
        binding.contentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = certAdapter
        }

        val data = mutableMapOf(
            context.getString(R.string.personal_data_title) to "",
            context.getString(R.string.standardised_family_name_title) to certificateModel.person.standardisedFamilyName,
            context.getString(R.string.family_name_title) to certificateModel.person.familyName.orEmpty(),
            context.getString(R.string.standardised_given_name_title) to certificateModel.person.standardisedGivenName.orEmpty(),
            context.getString(R.string.given_name_title) to certificateModel.person.givenName.orEmpty(),
            context.getString(R.string.date_of_birth_title) to certificateModel.dateOfBirth,
        )
        certificateModel.vaccinations?.let {
            if (it.isNotEmpty()) {
                val item = it.first()
                data[context.getString(R.string.vaccination_title)] = ""
                data[context.getString(R.string.target_disease)] = item.disease.value
                data[context.getString(R.string.vaccine_title)] = item.vaccine
                data[context.getString(R.string.medical_product_title)] = item.medicinalProduct
                data[context.getString(R.string.manufacturer_title)] = item.manufacturer
                data[context.getString(R.string.dose_number_title)] = item.doseNumber.toString()
                data[context.getString(R.string.total_doses_title)] = item.totalSeriesOfDoses.toString()
                data[context.getString(R.string.date_of_vaccination_title)] = item.dateOfVaccination
                data[context.getString(R.string.country_of_vaccination_title)] = item.countryOfVaccination
                data[context.getString(R.string.certificate_issuer_title)] = item.certificateIssuer
                data[context.getString(R.string.certificate_identifier_title)] = item.certificateIdentifier
            }
        }
        certificateModel.tests?.let {
            if (it.isNotEmpty()) {
                val item = it.first()
                data[context.getString(R.string.test_title)] = ""
                data[context.getString(R.string.target_disease)] = item.disease.value
                data[context.getString(R.string.type_of_test_title)] = item.typeOfTest.value
                data[context.getString(R.string.test_name_title)] = item.testName.orEmpty()
                data[context.getString(R.string.test_name_manufacturer_title)] = item.testNameAndManufacturer.orEmpty()
                data[context.getString(R.string.date_time_of_collection_title)] = item.dateTimeOfCollection
                data[context.getString(R.string.date_time_of_test_result_title)] = item.dateTimeOfTestResult.orEmpty()
                data[context.getString(R.string.test_result_title)] = item.testResult
                data[context.getString(R.string.testing_centre_title)] = item.testingCentre
                data[context.getString(R.string.country_of_vaccination_title)] = item.countryOfVaccination
                data[context.getString(R.string.certificate_identifier_title)] = item.certificateIdentifier
                data[context.getString(R.string.result_type_title)] = item.resultType.value
            }
        }
        certificateModel.recoveryStatements?.let {
            if (it.isNotEmpty()) {
                val item = it.first()
                data[context.getString(R.string.recovery_title)] = ""
                data[context.getString(R.string.target_disease)] = item.disease.value
                data[context.getString(R.string.date_of_first_positive_test_title)] = item.dateOfFirstPositiveTest
                data[context.getString(R.string.country_of_vaccination_title)] = item.countryOfVaccination
                data[context.getString(R.string.certificate_issuer_title)] = item.certificateIssuer
                data[context.getString(R.string.certificate_valid_from_title)] = item.certificateValidFrom
                data[context.getString(R.string.certificate_valid_until_title)] = item.certificateValidUntil
                data[context.getString(R.string.certificate_identifier_title)] = item.certificateIdentifier
            }
        }

        certAdapter.updateData(data)
        setExpanded(false)
    }

    private fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        binding.expandButton.setImageResource(if (expanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
        binding.contentRecyclerView.isVisible = isExpanded
    }
}