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
 *  Created by SchulzeStTSI on 22.06.21, 00:13
 */
package dgca.verifier.app.android

import dgca.verifier.app.android.verification.InnerVerificationResult
import dgca.verifier.app.android.verification.VerificationError
import dgca.verifier.app.android.verification.VerificationViewModel.Companion.validateCertData
import dgca.verifier.app.android.verification.fetchError
import dgca.verifier.app.decoder.model.*
import org.junit.Assert
import org.junit.Test
import java.util.*

class CertificateTests {
    @Test
    fun TestRecoveryValidity() {
        val result = VerificationResult()
        result.base45Decoded = true
        result.cborDecoded = true
        result.contextPrefix = "HC1"
        result.coseVerified = true
        result.isIssuedTimeCorrect = true
        result.isSchemaValid = true
        result.isNotExpired = true
        result.zlibDecoded = true
        val statement: MutableList<RecoveryStatement> = ArrayList()
        statement.add(RecoveryStatement("", "", "", "", "", "2020-06-01", ""))
        var certificate = GreenCertificate(
            "1.0.0",
            Person(
                "HORST",
                "DIETER",
                "CLAUS",
                "TEST"
            ),
            "2012-01-01", null, null, statement
        )
        validateCertData(certificate, result)
        Assert.assertFalse(result.isValid())
        Assert.assertTrue(result.recoveryVerification!!.isNotValidAnymore)
        statement.clear()
        statement.add(RecoveryStatement("", "", "", "", "", "2097-06-01", ""))
        certificate = GreenCertificate(
            "1.0.0",
            Person(
                "HORST",
                "DIETER",
                "CLAUS",
                "TEST"
            ),
            "2060-01-01", null, null, statement
        )
        validateCertData(certificate, result)
        Assert.assertTrue(result.isValid())
        Assert.assertFalse(result.recoveryVerification!!.isNotValidAnymore)
    }

    @Test
    fun TestNoPublicKeysFound() {
        val result = VerificationResult(isNotExpired = true)
        result.isIssuedTimeCorrect = true
        val error = result.fetchError(InnerVerificationResult(noPublicKeysFound = true))
        Assert.assertTrue(error === VerificationError.VERIFICATION_FAILED)
    }

    @Test
    fun TestGreenCertExpired() {
        val result = VerificationResult()
        result.coseVerified = true
        result.isNotExpired = false
        val error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.GREEN_CERTIFICATE_EXPIRED)
    }

    @Test
    fun TestCertExpired() {
        val result = VerificationResult()
        result.coseVerified = true
        result.isNotExpired = true
        val error = result.fetchError(
            InnerVerificationResult(
                noPublicKeysFound = false,
                certificateExpired = true
            )
        )
        Assert.assertTrue(error === VerificationError.CERTIFICATE_EXPIRED)
    }

    @Test
    fun TestSignatureInvalid() {
        val result = VerificationResult(isNotExpired = true, coseVerified = true)
        val error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID)
    }

    @Test
    fun TestVerificationFailed() {
        var result =
            VerificationResult(isNotExpired = true, coseVerified = true, base45Decoded = false)
        result.isNotExpired = true
        result.coseVerified = true
        result.base45Decoded = false
        var error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID)
        result = result.copy(base45Decoded = true, cborDecoded = false)
        error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID)
        result = result.copy(cborDecoded = true, isSchemaValid = false)
        error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID)
    }

    @Test
    fun TestRecoveryExpiration() {
        val result = VerificationResult()
        result.isNotExpired = true
        result.coseVerified = true
        result.recoveryVerification = RecoveryVerificationResult(false, true)
        val error = result.fetchError(InnerVerificationResult(noPublicKeysFound = false))
        Assert.assertTrue(error === VerificationError.RECOVERY_NOT_VALID_ANYMORE)
    }
}