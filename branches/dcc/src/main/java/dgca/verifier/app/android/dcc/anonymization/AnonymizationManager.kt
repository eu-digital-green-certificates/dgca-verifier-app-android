/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 2:39 PM
 */

package dgca.verifier.app.android.dcc.anonymization

import dgca.verifier.app.android.dcc.model.CertificateModel
import dgca.verifier.app.android.dcc.settings.debug.mode.DebugModeState
import javax.inject.Inject

class AnonymizationManager @Inject constructor() {

    fun anonymizeDcc(certificateModel: CertificateModel, state: DebugModeState = DebugModeState.LEVEL_1): CertificateModel {
        return when (state) {
            DebugModeState.LEVEL_1 -> l1Anonymization(certificateModel)
            DebugModeState.LEVEL_2 -> l2Anonymization(certificateModel)
            else -> certificateModel
        }
    }

    private fun l1Anonymization(certificateModel: CertificateModel): CertificateModel {
        val person = certificateModel.person

        val dobAnonymizePart = certificateModel.dateOfBirth.substring(certificateModel.dateOfBirth.indexOf("-") + 1).anonymize()
        val dateOfBirth = certificateModel.dateOfBirth.replaceAfter("-", dobAnonymizePart)

        val vaccinations = certificateModel.vaccinations?.map {
            it.copy(certificateIdentifier = it.certificateIdentifier.anonymizeCi())
        }
        val tests = certificateModel.tests?.map {
            val dateTime = it.dateTimeOfCollection
            val timePart = dateTime.substring(dateTime.indexOf("T") + 1).anonymize()
            val dateTimeOfCollectionAnonymize = dateTime.replaceAfter("T", timePart)

            it.copy(
                dateTimeOfCollection = dateTimeOfCollectionAnonymize,
                certificateIdentifier = it.certificateIdentifier.anonymizeCi()
            )
        }

        val recovery = certificateModel.recoveryStatements?.map {
            it.copy(certificateIdentifier = it.certificateIdentifier.anonymizeCi())
        }

        return certificateModel.copy(
            person = certificateModel.person.copy(
                standardisedFamilyName = person.standardisedFamilyName.anonymize(),
                familyName = person.familyName?.anonymize(),
                standardisedGivenName = person.standardisedGivenName?.anonymize(),
                givenName = person.givenName?.anonymize(),
            ),
            dateOfBirth = dateOfBirth,
            vaccinations = vaccinations,
            tests = tests,
            recoveryStatements = recovery
        )
    }

    private fun l2Anonymization(certificateModel: CertificateModel): CertificateModel {
        val person = certificateModel.person

        val dobAnonymizePart = certificateModel.dateOfBirth.substring(certificateModel.dateOfBirth.indexOf("-") + 1).anonymize()
        val dateOfBirth = certificateModel.dateOfBirth.replaceAfter("-", dobAnonymizePart)

        return certificateModel.copy(
            person = certificateModel.person.copy(
                standardisedFamilyName = person.standardisedFamilyName.anonymize(),
                familyName = person.familyName?.anonymize(),
                standardisedGivenName = person.standardisedGivenName?.anonymize(),
                givenName = person.givenName?.anonymize(),
            ),
            dateOfBirth = dateOfBirth
        )
    }
}

fun String.anonymize(): String {
    val strBuilder = StringBuilder()
    forEach {
        val charReplaced = when (it.category) {
            CharCategory.LOWERCASE_LETTER -> "x"
            CharCategory.TITLECASE_LETTER, CharCategory.UPPERCASE_LETTER -> "X"
            CharCategory.MODIFIER_LETTER -> "M"
            CharCategory.OTHER_LETTER -> "R"
            CharCategory.COMBINING_SPACING_MARK -> "S"
            CharCategory.ENCLOSING_MARK, CharCategory.NON_SPACING_MARK -> "s"
            CharCategory.DECIMAL_DIGIT_NUMBER -> if ("[0-9]".toRegex().matches(it.toString())) "9" else "8"
            CharCategory.OTHER_NUMBER -> "2"
            CharCategory.LETTER_NUMBER -> "1"
            CharCategory.CURRENCY_SYMBOL, CharCategory.MODIFIER_SYMBOL, CharCategory.MATH_SYMBOL, CharCategory.OTHER_SYMBOL -> "@"

            else -> {
                when {
                    "\\p{P}".toRegex().matches(it.toString()) -> {
                        when {
                            "\\u002D".toRegex().matches(it.toString()) -> "-"
                            "\\u002E".toRegex().matches(it.toString()) -> "."
                            "\\u002C".toRegex().matches(it.toString()) -> ","
                            it.category == CharCategory.DASH_PUNCTUATION -> "="
                            it.category == CharCategory.FINAL_QUOTE_PUNCTUATION || it.category == CharCategory.START_PUNCTUATION ||
                                    it.category == CharCategory.INITIAL_QUOTE_PUNCTUATION || it.category == CharCategory.END_PUNCTUATION -> "Q"
                            else -> "!"
                        }
                    }
                    "\\p{Z}".toRegex().matches(it.toString()) -> {
                        when {
                            "\\u0020".toRegex().matches(it.toString()) -> " "
                            it.category == CharCategory.SPACE_SEPARATOR -> "_"
                            it.category == CharCategory.LINE_SEPARATOR || it.category == CharCategory.PARAGRAPH_SEPARATOR -> "N"
                            else -> "?"
                        }
                    }
                    else -> "Q"
                }
            }
        }

        strBuilder.append(charReplaced)
    }

    return strBuilder.toString()
}

fun String.anonymizeCi(): String {
    val ciPart = substring(lastIndexOf(":") + 1)
    val ciAnonymize = "[A-Za-z0-9]".toRegex().replace(ciPart, "X")

    return replaceAfterLast(":", ciAnonymize)
}
