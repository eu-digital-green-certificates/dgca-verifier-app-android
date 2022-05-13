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
 *  Created by mykhailo.nester on 5/5/21 11:07 PM
 */

package dgca.verifier.app.android.diia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CertificateModel(
    val person: PersonModel,
    val uaCertificateModel: UaCertificateModel?,
    val dateOfBirth: String,
    val vaccinations: List<VaccinationModel>?,
    val tests: List<TestModel>?,
    val recoveryStatements: List<RecoveryModel>?
) : Parcelable {

    fun getFullName(): String {
        val givenName: String? = person.givenName?.trim()
        val familyName: String? = person.familyName?.trim()
        val stringBuilder = StringBuilder()
        if (givenName?.isNotEmpty() == true) {
            stringBuilder.append(givenName)
        }

        if (familyName?.isNotEmpty() == true) {
            stringBuilder.append(" ").append(familyName)
        }

        if (stringBuilder.isEmpty()) {
            val standardisedGivenName = person.standardisedGivenName
            if (standardisedGivenName?.isNotEmpty() == true) {
                stringBuilder.append(standardisedGivenName)
            }

            val standardisedFamilyName = person.standardisedFamilyName
            if (standardisedFamilyName.isNotEmpty()) {
                stringBuilder.append(" ").append(standardisedFamilyName)
            }
        }

        return stringBuilder.trim().toString()
    }
}


@Parcelize
data class PersonModel(
    val standardisedFamilyName: String,
    val familyName: String?,
    val standardisedGivenName: String?,
    val givenName: String?
) : Parcelable

@Parcelize
data class UaCertificateModel(
    val rnokpp: String,
    val docType: String,
    val birthday: String,
    val genderEN: String,
    val genderUA: String,
    val docNumber: String,
    val lastNameEN: String,
    val lastNameUA: String,
    val firstNameEN: String,
    val firstNameUA: String,
    val middleNameUA: String,
    val registration: String,
    val nationalityEN: String,
    val nationalityUA: String
) : Parcelable

@Parcelize
data class VaccinationModel(
    override val disease: DiseaseType,
    val vaccine: String,
    val medicinalProduct: String,
    val manufacturer: String,
    val doseNumber: Int,
    val totalSeriesOfDoses: Int,
    val dateOfVaccination: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String
) : CertificateData, Parcelable

@Parcelize
data class TestModel(
    override val disease: DiseaseType,
    val typeOfTest: TypeOfTest,
    val testName: String?,
    val testNameAndManufacturer: String?,
    val dateTimeOfCollection: String,
    val dateTimeOfTestResult: String?,
    val testResult: String,
    val testingCentre: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateIdentifier: String,
    val resultType: TestResult
) : CertificateData, Parcelable

enum class TestResult(val value: String) {
    DETECTED("DETECTED"),
    NOT_DETECTED("NOT DETECTED")
}

enum class DiseaseType(val value: String) {
    COVID_19("COVID-19"),
    UNDEFINED("UNDEFINED")
}

enum class TypeOfTest(val value: String) {
    NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION("Nucleic acid amplification with probe detection"),
    RAPID_IMMUNOASSAY("Rapid immunoassay"),
    UNDEFINED("")
}

@Parcelize
data class RecoveryModel(
    override val disease: DiseaseType,
    val dateOfFirstPositiveTest: String,
    val countryOfVaccination: String,
    val certificateIssuer: String,
    val certificateValidFrom: String,
    val certificateValidUntil: String,
    val certificateIdentifier: String
) : CertificateData, Parcelable

interface CertificateData {
    val disease: DiseaseType
}
