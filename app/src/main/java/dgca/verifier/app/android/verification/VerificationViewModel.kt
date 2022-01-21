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
 *  Created by osarapulov on 9/2/21 9:36 AM
 */

package dgca.verifier.app.android.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcc.app.revocation.domain.getDccSignatureSha256
import dcc.app.revocation.domain.model.DccRevokationDataHolder
import dcc.app.revocation.domain.toSha256HexString
import dcc.app.revocation.domain.usacase.IsDccRevokedUseCase
import dgca.verifier.app.android.data.VerifierRepository
import dgca.verifier.app.android.data.local.Preferences
import dgca.verifier.app.android.model.rules.toRuleValidationResultModels
import dgca.verifier.app.android.model.toCertificateModel
import dgca.verifier.app.android.settings.debug.mode.DebugModeState
import dgca.verifier.app.android.verification.*
import dgca.verifier.app.android.verification.model.*
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.GreenCertificateData
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.*
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.*
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.cert.X509Certificate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val prefixValidationService: PrefixValidationService,
    private val base45Service: Base45Service,
    private val compressorService: CompressorService,
    private val cryptoService: CryptoService,
    private val coseService: CoseService,
    private val schemaValidator: SchemaValidator,
    private val cborService: CborService,
    private val verifierRepository: VerifierRepository,
    private val engine: CertLogicEngine,
    private val getRulesUseCase: GetRulesUseCase,
    private val valueSetsRepository: ValueSetsRepository,
    private val preferences: Preferences,
    private val isDccRevokedUseCase: IsDccRevokedUseCase
) : ViewModel() {

    private val _qrCodeVerificationResult = MutableLiveData<QrCodeVerificationResult>()
    val qrCodeVerificationResult: LiveData<QrCodeVerificationResult> = _qrCodeVerificationResult

    fun init(qrCodeText: String, countryIsoCode: String) {
        decode(qrCodeText, countryIsoCode)
    }

    private fun decode(code: String, countryOfArrivalIsoCode: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val verificationResult = VerificationResult()
                val innerVerificationResult: InnerVerificationResult = validateCertificate(code, verificationResult)

                val validationResults: List<ValidationResult>? =
                    if (verificationResult.isValid() && innerVerificationResult.base64EncodedKid?.isNotBlank() == true) {
                        innerVerificationResult.greenCertificateData?.validateRules(
                            verificationResult,
                            innerVerificationResult.certificateIssuingCountryIsoCode.orEmpty(),
                            countryOfArrivalIsoCode,
                            innerVerificationResult.base64EncodedKid
                        )
                    } else {
                        null
                    }

                if (innerVerificationResult.isApplicableCode) {
                    val covidCertificate = innerVerificationResult.greenCertificateData?.greenCertificate
                    val certificateModel = covidCertificate?.toCertificateModel()
                    val hcert: String? = innerVerificationResult.greenCertificateData?.hcertJson
                    val standardizedVerificationResult: StandardizedVerificationResult =
                        extractStandardizedVerificationResultFrom(
                            verificationResult,
                            innerVerificationResult
                        )

                    val isDebugModeEnabled =
                        standardizedVerificationResult.category != StandardizedVerificationResultCategory.VALID
                                && (preferences.debugModeState?.let { DebugModeState.valueOf(it) }
                            ?: DebugModeState.OFF) != DebugModeState.OFF
                                && preferences.debugModeSelectedCountriesCodes?.contains(
                            innerVerificationResult.greenCertificateData?.getNormalizedIssuingCountry()
                        ) == true

                    QrCodeVerificationResult.Applicable(
                        standardizedVerificationResult,
                        certificateModel,
                        hcert,
                        validationResults?.toRuleValidationResultModels(),
                        isDebugModeEnabled,
                        innerVerificationResult.debugData
                    )
                } else {
                    QrCodeVerificationResult.NotApplicable
                }
            }.let { qrCodeVerificationResultInner ->
                _qrCodeVerificationResult.value = qrCodeVerificationResultInner
            }
        }
    }

    private suspend fun validateCertificate(
        code: String,
        verificationResult: VerificationResult
    ): InnerVerificationResult {
        var greenCertificateData: GreenCertificateData? = null
        var isApplicableCode = false

        val plainInput = prefixValidationService.decode(code, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        if (verificationResult.base45Decoded.not()) {
            Timber.d("Verification failed: base45 not decoded")
            return InnerVerificationResult(
                greenCertificateData = greenCertificateData,
                isApplicableCode = isApplicableCode
            )
        }

        val cose: ByteArray? = compressorService.decode(compressedCose, verificationResult)
        if (cose == null) {
            Timber.d("Verification failed: Too many bytes read")
            return InnerVerificationResult(
                greenCertificateData = greenCertificateData,
                isApplicableCode = isApplicableCode
            )
        }

        val coseData = coseService.decode(cose, verificationResult)
        if (coseData == null) {
            Timber.d("Verification failed: COSE not decoded")
            return InnerVerificationResult(
                greenCertificateData = greenCertificateData,
                isApplicableCode = isApplicableCode
            )
        }

        val kid = coseData.kid
        if (kid == null) {
            Timber.d("Verification failed: cannot extract kid from COSE")
            return InnerVerificationResult(
                greenCertificateData = greenCertificateData,
                isApplicableCode = isApplicableCode
            )
        }

        isApplicableCode = true

        schemaValidator.validate(coseData.cbor, verificationResult)
        greenCertificateData = cborService.decodeData(coseData.cbor, verificationResult)
        validateCertData(greenCertificateData?.greenCertificate, verificationResult)

        val base64EncodedKid = kid.toBase64()
        val certificates = verifierRepository.getCertificatesBy(base64EncodedKid)
        if (certificates.isEmpty()) {
            Timber.d("Verification failed: failed to load certificate")
            return InnerVerificationResult(
                greenCertificateData = greenCertificateData,
                isApplicableCode = isApplicableCode,
                base64EncodedKid = base64EncodedKid
            )
        }
        val noPublicKeysFound = false
        var certificateExpired = false
        var certificateRevoked = false
        var issuingCountry = ""
        certificates.forEach { innerCertificate ->
            cryptoService.validate(
                cose,
                innerCertificate,
                verificationResult,
                greenCertificateData?.greenCertificate?.getType()
                    ?: dgca.verifier.app.decoder.model.CertificateType.UNKNOWN
            )

            if (verificationResult.coseVerified) {
                var expirationTime: ZonedDateTime? = null
                if (innerCertificate is X509Certificate) {
                    expirationTime = innerCertificate.notAfter.toInstant().atZone(UTC_ZONE_ID)
                    issuingCountry = innerCertificate.getIssuerCountry()
                }

                val currentTime: ZonedDateTime = ZonedDateTime.now().withZoneSameInstant(UTC_ZONE_ID)

                if (expirationTime != null && currentTime.isAfter(expirationTime)) {
                    certificateExpired = true
                }

                certificateRevoked = isDCCRevoked(base64EncodedKid, greenCertificateData?.greenCertificate, cose)

                return@forEach
            }
        }

        return InnerVerificationResult(
            noPublicKeysFound = noPublicKeysFound,
            certificateExpired = certificateExpired,
            certificateIssuingCountryIsoCode = issuingCountry,
            greenCertificateData = greenCertificateData,
            isApplicableCode = isApplicableCode,
            base64EncodedKid = base64EncodedKid,
            debugData = DebugData(code, cose, coseData.cbor),
            certificateRevoked = certificateRevoked
        )
    }

    private suspend fun isDCCRevoked(kid: String, greenCertificate: GreenCertificate?, cose: ByteArray): Boolean {
        greenCertificate ?: return false

        greenCertificate.vaccinations?.firstOrNull()?.let {
            val uvciSha256 = it.certificateIdentifier.toByteArray().toSha256HexString()
            val coUvciSha256 = (it.countryOfVaccination + it.certificateIdentifier).toByteArray().toSha256HexString()
            val signatureSha256 = cose.getDccSignatureSha256()

            val data = DccRevokationDataHolder(
                kid,
                uvciSha256,
                coUvciSha256,
                signatureSha256
            )

//            TODO: check lookup mode
            val result = isDccRevokedUseCase.execute(data)

        }

        return true
    }

    private suspend fun GreenCertificateData.validateRules(
        verificationResult: VerificationResult,
        certificateIssuingCountryIsoCode: String,
        countryOfArrivalIsoCode: String,
        base64EncodedKid: String
    ): List<ValidationResult>? {
        this.apply {
            val engineCertificateType = this.greenCertificate.getEngineCertificateType()

            return if (countryOfArrivalIsoCode.isNotBlank()) {
                val issuingCountry: String = if (certificateIssuingCountryIsoCode.isNotBlank()) {
                    certificateIssuingCountryIsoCode
                } else {
                    this.getNormalizedIssuingCountry()
                }
                val rules = getRulesUseCase.invoke(
                    ZonedDateTime.now().withZoneSameInstant(UTC_ZONE_ID),
                    countryOfArrivalIsoCode,
                    issuingCountry,
                    engineCertificateType
                )
                val valueSetsMap = mutableMapOf<String, List<String>>()
                valueSetsRepository.getValueSets().forEach { valueSet ->
                    val ids = mutableListOf<String>()
                    valueSet.valueSetValues.fieldNames().forEach { id ->
                        ids.add(id)
                    }
                    valueSetsMap[valueSet.valueSetId] = ids
                }

                val externalParameter = ExternalParameter(
                    validationClock = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.id)),
                    valueSets = valueSetsMap,
                    countryCode = countryOfArrivalIsoCode,
                    exp = this.expirationTime,
                    iat = this.issuedAt,
                    issuerCountryCode = issuingCountry,
                    kid = base64EncodedKid,
                    region = "",
                )
                val validationResults = engine.validate(
                    engineCertificateType,
                    this.greenCertificate.schemaVersion,
                    rules,
                    externalParameter,
                    this.hcertJson
                )

                validationResults.forEach {
                    if (it.result != Result.PASSED) {
                        verificationResult.rulesValidationFailed = true
                        return@forEach
                    }
                }

                validationResults
            } else {
                null
            }
        }
    }

    private fun GreenCertificate.getEngineCertificateType(): CertificateType {
        return when {
            this.recoveryStatements?.isNotEmpty() == true -> CertificateType.RECOVERY
            this.vaccinations?.isNotEmpty() == true -> CertificateType.VACCINATION
            this.tests?.isNotEmpty() == true -> CertificateType.TEST
            else -> CertificateType.TEST
        }
    }

    companion object {

        fun validateCertData(
            certificate: GreenCertificate?,
            verificationResult: VerificationResult
        ) {
            certificate?.vaccinations?.let {
                if (it.isNotEmpty()) {
                    val vaccination = it.first()
                    verificationResult.vaccinationVerification = VaccinationVerificationResult(vaccination.isDateInThePast())
                }
            }

            certificate?.tests?.let {
                if (it.isNotEmpty()) {
                    val test = it.first()
                    verificationResult.testVerification = TestVerificationResult(test.isResultNegative(), test.isDateInThePast())
                }
            }
            certificate?.recoveryStatements?.let {
                if (it.isNotEmpty()) {
                    val recovery = it.first()
                    verificationResult.recoveryVerification =
                        RecoveryVerificationResult(
                            recovery.isCertificateNotValidSoFar() == true,
                            recovery.isCertificateNotValidAnymore() == true
                        )
                }
            }
        }
    }
}

const val ISSUING_COUNTRY_X509_CERTIFICATE_KEY = "C"

fun X509Certificate.getIssuerCountry(): String {
    val fieldPattern = Pattern.compile("$ISSUING_COUNTRY_X509_CERTIFICATE_KEY=((?:[^,]|\\\\,)+)");
    val matcher = fieldPattern.matcher(issuerX500Principal.name);
    if (matcher.find()) {
        return matcher.group(1);
    }
    return ""
}