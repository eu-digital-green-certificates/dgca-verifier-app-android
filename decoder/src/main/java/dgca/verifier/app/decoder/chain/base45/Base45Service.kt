package dgca.verifier.app.decoder.chain.base45

import dgca.verifier.app.decoder.chain.model.VerificationResult

interface Base45Service {

    fun decode(input: String, verificationResult: VerificationResult): ByteArray
}