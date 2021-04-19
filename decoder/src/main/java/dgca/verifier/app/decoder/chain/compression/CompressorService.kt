package dgca.verifier.app.decoder.chain.compression

import dgca.verifier.app.decoder.chain.model.VerificationResult

interface CompressorService {

    fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray
}