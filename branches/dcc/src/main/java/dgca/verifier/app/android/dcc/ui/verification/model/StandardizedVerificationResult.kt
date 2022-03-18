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
 *  Created by mykhailo.nester on 10/10/2021, 09:19
 */

package dgca.verifier.app.android.dcc.ui.verification.model

import dgca.verifier.app.decoder.model.VerificationResult

enum class StandardizedVerificationResultCategory {
    VALID, LIMITED_VALIDITY, INVALID
}

enum class StandardizedVerificationResult(val category: StandardizedVerificationResultCategory = StandardizedVerificationResultCategory.INVALID) {

    SUCCESS(StandardizedVerificationResultCategory.VALID),

    RULES_VALIDATION_FAILED(StandardizedVerificationResultCategory.LIMITED_VALIDITY),

    GREEN_CERTIFICATE_EXPIRED(StandardizedVerificationResultCategory.INVALID),
    CERTIFICATE_EXPIRED(StandardizedVerificationResultCategory.INVALID),
    CERTIFICATE_REVOKED(StandardizedVerificationResultCategory.INVALID),
    VERIFICATION_FAILED(StandardizedVerificationResultCategory.INVALID),
    VACCINATION_DATE_IS_IN_THE_FUTURE(StandardizedVerificationResultCategory.INVALID),
    TEST_DATE_IS_IN_THE_FUTURE(StandardizedVerificationResultCategory.INVALID),
    TEST_RESULT_POSITIVE(StandardizedVerificationResultCategory.INVALID),
    RECOVERY_NOT_VALID_SO_FAR(StandardizedVerificationResultCategory.INVALID),
    RECOVERY_NOT_VALID_ANYMORE(StandardizedVerificationResultCategory.INVALID),
    CRYPTOGRAPHIC_SIGNATURE_INVALID(StandardizedVerificationResultCategory.INVALID)
}

fun extractStandardizedVerificationResultFrom(
    verificationResult: VerificationResult,
    innerVerificationResult: InnerVerificationResult
): StandardizedVerificationResult =
    when {
        verificationResult.isValid() && innerVerificationResult.isValid() -> StandardizedVerificationResult.SUCCESS
        innerVerificationResult.noPublicKeysFound -> StandardizedVerificationResult.VERIFICATION_FAILED
        innerVerificationResult.certificateExpired -> StandardizedVerificationResult.CERTIFICATE_EXPIRED
        !verificationResult.coseVerified -> StandardizedVerificationResult.CRYPTOGRAPHIC_SIGNATURE_INVALID
        !verificationResult.isNotExpired -> StandardizedVerificationResult.GREEN_CERTIFICATE_EXPIRED
        verificationResult.isVaccinationDateInTheFuture() -> StandardizedVerificationResult.VACCINATION_DATE_IS_IN_THE_FUTURE
        verificationResult.isTestDateInTheFuture() -> StandardizedVerificationResult.TEST_DATE_IS_IN_THE_FUTURE
        verificationResult.isTestWithPositiveResult() -> StandardizedVerificationResult.TEST_RESULT_POSITIVE
        verificationResult.isRecoveryNotValidSoFar() -> StandardizedVerificationResult.RECOVERY_NOT_VALID_SO_FAR
        verificationResult.isRecoveryNotValidAnymore() -> StandardizedVerificationResult.RECOVERY_NOT_VALID_ANYMORE
        verificationResult.rulesValidationFailed -> StandardizedVerificationResult.RULES_VALIDATION_FAILED
        else -> StandardizedVerificationResult.CRYPTOGRAPHIC_SIGNATURE_INVALID
    }