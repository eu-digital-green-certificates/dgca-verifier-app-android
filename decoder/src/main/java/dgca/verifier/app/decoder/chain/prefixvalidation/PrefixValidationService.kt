package dgca.verifier.app.decoder.chain.prefixvalidation

import dgca.verifier.app.decoder.chain.model.VerificationResult

interface PrefixValidationService {

    fun decode(input: String, verificationResult: VerificationResult): String
}