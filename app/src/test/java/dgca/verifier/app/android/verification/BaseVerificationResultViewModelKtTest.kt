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
 *  Created by osarapulov on 7/9/21 9:57 AM
 */

package dgca.verifier.app.android.verification

import dgca.verifier.app.android.verification.model.InnerVerificationResult
import dgca.verifier.app.android.verification.model.StandardizedVerificationResultCategory
import dgca.verifier.app.android.verification.model.extractStandardizedVerificationResultFrom
import dgca.verifier.app.decoder.cbor.GreenCertificateData
import dgca.verifier.app.decoder.model.GreenCertificate
import dgca.verifier.app.decoder.model.Person
import dgca.verifier.app.decoder.model.TestVerificationResult
import dgca.verifier.app.decoder.model.VerificationResult
import dgca.verifier.app.engine.UTC_ZONE_ID
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZonedDateTime

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 09.07.21 9:57
 */
class BaseVerificationResultViewModelKtTest {

    @Test
    fun testGetGeneralResultSuccess() {
        val greenCertificateData = greenCertificateData()
        val res = extractStandardizedVerificationResultFrom(
            VerificationResult(
                base45Decoded = true,
                zlibDecoded = true,
                coseVerified = true,
                cborDecoded = true,
                isSchemaValid = true,
                isIssuedTimeCorrect = true,
                isNotExpired = true,
                testVerification = TestVerificationResult(isTestResultNegative = true, isTestDateInThePast = true)
            ),
            InnerVerificationResult(
                noPublicKeysFound = false,
                greenCertificateData = greenCertificateData,
                isApplicableCode = true,
                base64EncodedKid = "base64EncodedKid"
            )
        )

        assertEquals(StandardizedVerificationResultCategory.VALID, res.category)
    }

    private fun person(): Person {
        return Person("", null, null, null)
    }

    private fun greenCertificate(person: Person = person()): GreenCertificate {
        return GreenCertificate("", person, "", null, null, null)
    }

    private fun greenCertificateData(greenCertificate: GreenCertificate = greenCertificate()): GreenCertificateData {
        return GreenCertificateData(
            null, "", greenCertificate, ZonedDateTime.now().withZoneSameInstant(
                UTC_ZONE_ID
            ), ZonedDateTime.now().withZoneSameInstant(
                UTC_ZONE_ID
            )
        )
    }

    @Test
    fun testGetGeneralResultTestResultDetected() {
        val res = extractStandardizedVerificationResultFrom(
            VerificationResult(
                base45Decoded = true,
                zlibDecoded = true,
                coseVerified = true,
                cborDecoded = true,
                isSchemaValid = true,
                isIssuedTimeCorrect = true,
                isNotExpired = true,
                testVerification = TestVerificationResult(isTestResultNegative = false, isTestDateInThePast = true),
                rulesValidationFailed = true
            ), InnerVerificationResult()
        )

        assertEquals(StandardizedVerificationResultCategory.INVALID, res.category)
    }

    @Test
    fun testGetGeneralResultRulesValidationFailed() {
        val res = extractStandardizedVerificationResultFrom(
            VerificationResult(
                base45Decoded = true,
                zlibDecoded = true,
                coseVerified = true,
                cborDecoded = true,
                isSchemaValid = true,
                isIssuedTimeCorrect = true,
                isNotExpired = true,
                testVerification = TestVerificationResult(isTestResultNegative = true, isTestDateInThePast = true),
                rulesValidationFailed = true
            ), InnerVerificationResult(noPublicKeysFound = false)
        )

        assertEquals(
            StandardizedVerificationResultCategory.LIMITED_VALIDITY,
            res.category
        )
    }

    @Test
    fun testGetGeneralResultValidationFailed() {
        val res = extractStandardizedVerificationResultFrom(
            VerificationResult(
                base45Decoded = true,
                zlibDecoded = true,
                coseVerified = true,
                cborDecoded = true,
                isSchemaValid = false,
                isIssuedTimeCorrect = true,
                isNotExpired = true,
                testVerification = TestVerificationResult(isTestResultNegative = true, isTestDateInThePast = true),
                rulesValidationFailed = false
            ), InnerVerificationResult()
        )

        assertEquals(StandardizedVerificationResultCategory.INVALID, res.category)
    }
}