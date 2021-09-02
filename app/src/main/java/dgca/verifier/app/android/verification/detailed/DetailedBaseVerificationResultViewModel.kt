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
 *  Created by osarapulov on 8/31/21 10:49 AM
 */

package dgca.verifier.app.android.verification.detailed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.data.VerifierRepository
import dgca.verifier.app.android.verification.BaseVerificationResultViewModel
import dgca.verifier.app.android.verification.DecodeResult
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import javax.inject.Inject

enum class VerificationComponent { TECHNICAL_VERIFICATION, ISSUER_INVALIDATION, DESTINATION_INVALIDATION, TRAVELLER_ACCEPTANCE }

enum class VerificationComponentState { PASSED, FAILED, OPEN }

enum class VerificationResult { VALID, INVALID, LIMITED_VALIDITY }

fun Map<VerificationComponent, VerificationComponentState>.toVerificationResult(): VerificationResult =
    when {
        this[VerificationComponent.TECHNICAL_VERIFICATION] != VerificationComponentState.PASSED || this[VerificationComponent.TRAVELLER_ACCEPTANCE] != VerificationComponentState.PASSED -> VerificationResult.INVALID
        this[VerificationComponent.ISSUER_INVALIDATION] == VerificationComponentState.PASSED && this[VerificationComponent.DESTINATION_INVALIDATION] == VerificationComponentState.PASSED -> VerificationResult.VALID
        else -> VerificationResult.LIMITED_VALIDITY
    }

@HiltViewModel
class DetailedBaseVerificationResultViewModel @Inject constructor(
    prefixValidationService: PrefixValidationService,
    base45Service: Base45Service,
    compressorService: CompressorService,
    cryptoService: CryptoService,
    coseService: CoseService,
    schemaValidator: SchemaValidator,
    cborService: CborService,
    verifierRepository: VerifierRepository,
    engine: CertLogicEngine,
    getRulesUseCase: GetRulesUseCase,
    valueSetsRepository: ValueSetsRepository
) : BaseVerificationResultViewModel(
    prefixValidationService,
    base45Service,
    compressorService,
    cryptoService,
    coseService,
    schemaValidator,
    cborService,
    verifierRepository,
    engine,
    getRulesUseCase,
    valueSetsRepository
) {
    private val _detailedVerificationResult = MutableLiveData<DetailedVerificationResult>()
    val detailedVerificationResult: LiveData<DetailedVerificationResult> =
        _detailedVerificationResult

    override fun handleDecodeResult(decodeResult: DecodeResult) {
        _detailedVerificationResult.value = decodeResult.toDetailedVerificationResult()
    }

    private fun DecodeResult.toDetailedVerificationResult(): DetailedVerificationResult {
        return DetailedVerificationResult(
            this.verificationData.certificateModel,
            this.verificationError
        )
    }
}