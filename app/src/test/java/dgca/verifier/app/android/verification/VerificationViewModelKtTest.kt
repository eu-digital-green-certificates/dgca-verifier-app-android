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

import dgca.verifier.app.decoder.model.TestVerificationResult
import dgca.verifier.app.decoder.model.VerificationResult
import org.junit.Assert.assertEquals
import org.junit.Test

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
class VerificationViewModelKtTest {
    @Test
    fun testGetGeneralResultSuccess() {
        val verificationResult = VerificationResult(
            base45Decoded = true,
            zlibDecoded = true,
            coseVerified = true,
            cborDecoded = true,
            isSchemaValid = true,
            isIssuedTimeCorrect = true,
            isNotExpired = true,
            testVerification = TestVerificationResult(true, true)
        )

        assertEquals(GeneralVerificationResult.SUCCESS, verificationResult.getGeneralResult())
    }

    @Test
    fun testGetGeneralResultTestResultDetected() {
        val verificationResult = VerificationResult(
            base45Decoded = true,
            zlibDecoded = true,
            coseVerified = true,
            cborDecoded = true,
            isSchemaValid = true,
            isIssuedTimeCorrect = true,
            isNotExpired = true,
            testVerification = TestVerificationResult(false, true),
            rulesValidationFailed = true
        )

        assertEquals(GeneralVerificationResult.FAILED, verificationResult.getGeneralResult())
    }

    @Test
    fun testGetGeneralResultRulesValidationFailed() {
        val verificationResult = VerificationResult(
            base45Decoded = true,
            zlibDecoded = true,
            coseVerified = true,
            cborDecoded = true,
            isSchemaValid = true,
            isIssuedTimeCorrect = true,
            isNotExpired = true,
            testVerification = TestVerificationResult(true, true),
            rulesValidationFailed = true
        )

        assertEquals(GeneralVerificationResult.RULES_VALIDATION_FAILED, verificationResult.getGeneralResult())
    }

    @Test
    fun testGetGeneralResultValidationFailed() {
        val verificationResult = VerificationResult(
            base45Decoded = true,
            zlibDecoded = true,
            coseVerified = true,
            cborDecoded = true,
            isSchemaValid = false,
            isIssuedTimeCorrect = true,
            isNotExpired = true,
            testVerification = TestVerificationResult(true, true),
            rulesValidationFailed = false
        )

        assertEquals(GeneralVerificationResult.FAILED, verificationResult.getGeneralResult())
    }
}