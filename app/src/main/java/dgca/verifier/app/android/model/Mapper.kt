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
 *  Created by mykhailo.nester on 5/5/21 11:17 PM
 */

package dgca.verifier.app.android.model

import dgca.verifier.app.decoder.model.*

fun GreenCertificate.toCertificateModel(): CertificateModel =
    CertificateModel(
        person = person.toPersonModel(),
        dateOfBirth = dateOfBirth,
        vaccinations = vaccinations?.map { it.toVaccinationModel() },
        tests = tests?.map { it.toTestModel() },
        recoveryStatements = recoveryStatements?.map { it.toRecoveryModel() }
    )

fun RecoveryStatement.toRecoveryModel(): RecoveryModel =
    RecoveryModel(
        disease = disease.toDiseaseCode().toDiseaseType(),
        dateOfFirstPositiveTest = dateOfFirstPositiveTest,
        countryOfVaccination = countryOfVaccination,
        certificateIssuer = certificateIssuer,
        certificateValidFrom = certificateValidFrom,
        certificateValidUntil = certificateValidUntil,
        certificateIdentifier = certificateIdentifier
    )

fun Test.toTestModel(): TestModel =
    TestModel(
        disease = disease.toDiseaseCode().toDiseaseType(),
        typeOfTest = typeOfTest.toTypeOfTestCode().toTypeOfTest(),
        testName = testName,
        testNameAndManufacturer = testNameAndManufacturer,
        dateTimeOfCollection = dateTimeOfCollection,
        dateTimeOfTestResult = dateTimeOfTestResult,
        testResult = testResult,
        testingCentre = testingCentre,
        countryOfVaccination = countryOfVaccination,
        certificateIssuer = certificateIssuer,
        certificateIdentifier = certificateIdentifier,
        resultType = getTestResultType().toTestResult()
    )

fun Test.TestResult.toTestResult(): TestResult =
    when (this) {
        Test.TestResult.DETECTED -> TestResult.DETECTED
        Test.TestResult.NOT_DETECTED -> TestResult.NOT_DETECTED
    }

fun DiseaseCode.toDiseaseType(): DiseaseType =
    when (this) {
        DiseaseCode.COVID_19 -> DiseaseType.COVID_19
        else -> DiseaseType.UNDEFINED
    }

fun TypeOfTestCode.toTypeOfTest(): TypeOfTest =
    when (this) {
        TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION -> TypeOfTest.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION
        TypeOfTestCode.RAPID_IMMUNOASSAY -> TypeOfTest.RAPID_IMMUNOASSAY
        else -> TypeOfTest.UNDEFINED
    }

fun Vaccination.toVaccinationModel(): VaccinationModel =
    VaccinationModel(
        disease = disease.toDiseaseCode().toDiseaseType(),
        vaccine = vaccine,
        medicinalProduct = medicinalProduct,
        manufacturer = manufacturer,
        doseNumber = doseNumber,
        totalSeriesOfDoses = totalSeriesOfDoses,
        dateOfVaccination = dateOfVaccination,
        countryOfVaccination = countryOfVaccination,
        certificateIssuer = certificateIssuer,
        certificateIdentifier = certificateIdentifier
    )

fun Person.toPersonModel(): PersonModel =
    PersonModel(
        standardisedFamilyName = standardisedFamilyName,
        familyName = familyName,
        standardisedGivenName = standardisedGivenName,
        givenName = givenName
    )

fun String.toDiseaseCode(): DiseaseCode =
    when (this) {
        DiseaseCode.COVID_19.value -> DiseaseCode.COVID_19
        else -> DiseaseCode.UNDEFINED
    }

fun String.toTypeOfTestCode(): TypeOfTestCode =
    when (this) {
        TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION.value -> TypeOfTestCode.NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION
        TypeOfTestCode.RAPID_IMMUNOASSAY.value -> TypeOfTestCode.RAPID_IMMUNOASSAY
        else -> TypeOfTestCode.UNDEFINED
    }

enum class DiseaseCode(val value: String) {
    COVID_19("840539006"),
    UNDEFINED("")
}

enum class TypeOfTestCode(val value: String) {
    NUCLEIC_ACID_AMPLIFICATION_WITH_PROBE_DETECTION("LP6464-4"),
    RAPID_IMMUNOASSAY("LP217198-3"),
    UNDEFINED("")
}