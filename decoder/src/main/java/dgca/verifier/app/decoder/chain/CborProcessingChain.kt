package dgca.verifier.app.decoder.chain

import dgca.verifier.app.decoder.chain.base45.Base45Service
import dgca.verifier.app.decoder.chain.cbor.CborService
import dgca.verifier.app.decoder.chain.compression.CompressorService
import dgca.verifier.app.decoder.chain.cose.CoseService
import dgca.verifier.app.decoder.chain.model.VaccinationData
import dgca.verifier.app.decoder.chain.model.VerificationResult
import dgca.verifier.app.decoder.chain.prefixvalidation.PrefixValidationService

class CborProcessingChain(
    private val cborService: CborService,
    private val coseService: CoseService,
    private val prefixValidationService: PrefixValidationService,
    private val compressorService: CompressorService,
    private val base45Service: Base45Service,
    private val schemaValidator: SchemaValidator
) {

    fun verify(
        input: String,
        verificationResult: VerificationResult = VerificationResult()
    ): VaccinationData {
        val plainInput = prefixValidationService.decode(input, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        val cose = compressorService.decode(compressedCose, verificationResult)
        val cbor = coseService.decode(cose, verificationResult)
        schemaValidator.validate(cbor, verificationResult)

        return cborService.decode(cbor, verificationResult)
    }
}