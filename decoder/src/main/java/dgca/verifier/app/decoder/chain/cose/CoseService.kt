package dgca.verifier.app.decoder.chain.cose

import dgca.verifier.app.decoder.chain.model.VerificationResult

interface CoseService {

    fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray
}