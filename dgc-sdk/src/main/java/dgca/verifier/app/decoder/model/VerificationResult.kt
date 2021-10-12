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
 *  Created by Mykhailo Nester on 4/23/21 9:51 AM
 */

package dgca.verifier.app.decoder.model

data class VerificationResult(
    var base45Decoded: Boolean = false,
    var contextPrefix: String? = null,
    var zlibDecoded: Boolean = false,
    var coseVerified: Boolean = false,
    var cborDecoded: Boolean = false,
    var isSchemaValid: Boolean = false,
    var isIssuedTimeCorrect: Boolean = false,
    var isNotExpired: Boolean = false,
    var rulesValidationFailed: Boolean = false,
    var testVerification: TestVerificationResult? = null,
    var recoveryVerification: RecoveryVerificationResult? = null
) {

    fun isValid(): Boolean {
        val isTestValid = testVerification?.isTestValid() ?: true
        val isRecoveryValid = recoveryVerification?.isRecoveryValid() ?: true;
        return base45Decoded && zlibDecoded && coseVerified && cborDecoded && isSchemaValid && isTestValid &&
                isIssuedTimeCorrect && isNotExpired && !rulesValidationFailed && isRecoveryValid
    }

    /**
     * Checks if test hasn't been taken yet.
     */
    fun isTestDateInTheFuture(): Boolean = if (testVerification == null) {
        false
    } else {
        !testVerification!!.isTestDateInThePast
    }

    /**
     * Checks if verification is for the test, and related test result is {@code positive).
     */
    fun isTestWithPositiveResult(): Boolean = if (testVerification == null) {
        false
    } else {
        !testVerification!!.isTestResultNegative
    }

    fun isRecoveryNotValidAnymore(): Boolean = if (recoveryVerification == null) {
        false
    } else {
        recoveryVerification!!.isNotValidAnymore
    }

    fun isRecoveryNotValidSoFar(): Boolean = if (recoveryVerification == null) {
        false
    } else {
        recoveryVerification!!.isNotValidSoFar
    }

    override fun toString(): String {
        return "VerificationResult: \n" +
                "base45Decoded: $base45Decoded \n" +
                "contextPrefix: $contextPrefix \n" +
                "zlibDecoded: $zlibDecoded \n" +
                "coseVerified: $coseVerified \n" +
                "cborDecoded: $cborDecoded \n" +
                "isSchemaValid: $isSchemaValid"
    }
}

data class TestVerificationResult(val isTestResultNegative: Boolean, val isTestDateInThePast: Boolean) {
    fun isTestValid(): Boolean = isTestResultNegative && isTestDateInThePast
}

data class RecoveryVerificationResult(val isNotValidSoFar: Boolean, val isNotValidAnymore: Boolean) {
    fun isRecoveryValid() = !isNotValidSoFar && !isNotValidAnymore
}