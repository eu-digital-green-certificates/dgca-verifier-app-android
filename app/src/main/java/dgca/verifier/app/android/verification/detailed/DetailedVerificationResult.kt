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
 *  Created by osarapulov on 9/1/21 9:08 AM
 */

package dgca.verifier.app.android.verification.detailed

import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.verification.StandardizedVerificationResult

data class DetailedVerificationResult(
    val certificateModel: CertificateModel?,
    val standardizedVerificationResult: StandardizedVerificationResult
)

fun StandardizedVerificationResult.toVerificationComponentStates(): Map<VerificationComponent, VerificationComponentState> =
    when (this) {
        StandardizedVerificationResult.GREEN_CERTIFICATE_EXPIRED, StandardizedVerificationResult.CERTIFICATE_EXPIRED,
        StandardizedVerificationResult.CERTIFICATE_REVOKED, StandardizedVerificationResult.VERIFICATION_FAILED,
        StandardizedVerificationResult.TEST_DATE_IS_IN_THE_FUTURE, StandardizedVerificationResult.TEST_RESULT_POSITIVE,
        StandardizedVerificationResult.RECOVERY_NOT_VALID_SO_FAR, StandardizedVerificationResult.RECOVERY_NOT_VALID_ANYMORE -> mapOf(
            VerificationComponent.TECHNICAL_VERIFICATION to VerificationComponentState.PASSED,
            VerificationComponent.ISSUER_INVALIDATION to VerificationComponentState.OPEN,
            VerificationComponent.DESTINATION_INVALIDATION to VerificationComponentState.OPEN,
            VerificationComponent.TRAVELLER_ACCEPTANCE to VerificationComponentState.FAILED
        )
        StandardizedVerificationResult.RULES_VALIDATION_FAILED -> mapOf(
            VerificationComponent.TECHNICAL_VERIFICATION to VerificationComponentState.PASSED,
            VerificationComponent.ISSUER_INVALIDATION to VerificationComponentState.FAILED,
            VerificationComponent.DESTINATION_INVALIDATION to VerificationComponentState.FAILED,
            VerificationComponent.TRAVELLER_ACCEPTANCE to VerificationComponentState.PASSED
        )
        StandardizedVerificationResult.CRYPTOGRAPHIC_SIGNATURE_INVALID -> mapOf(
            VerificationComponent.TECHNICAL_VERIFICATION to VerificationComponentState.FAILED,
            VerificationComponent.ISSUER_INVALIDATION to VerificationComponentState.OPEN,
            VerificationComponent.DESTINATION_INVALIDATION to VerificationComponentState.OPEN,
            VerificationComponent.TRAVELLER_ACCEPTANCE to VerificationComponentState.OPEN
        )
        else -> mapOf(
            VerificationComponent.TECHNICAL_VERIFICATION to VerificationComponentState.PASSED,
            VerificationComponent.ISSUER_INVALIDATION to VerificationComponentState.PASSED,
            VerificationComponent.DESTINATION_INVALIDATION to VerificationComponentState.PASSED,
            VerificationComponent.TRAVELLER_ACCEPTANCE to VerificationComponentState.PASSED
        )
    }