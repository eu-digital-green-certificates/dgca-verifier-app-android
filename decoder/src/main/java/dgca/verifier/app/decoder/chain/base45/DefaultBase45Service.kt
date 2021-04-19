package dgca.verifier.app.decoder.chain.base45

import dgca.verifier.app.decoder.chain.model.VerificationResult

@ExperimentalUnsignedTypes
class DefaultBase45Service : Base45Service {

    private val decoder = Base45Decoder()

    override fun decode(input: String, verificationResult: VerificationResult): ByteArray {
        verificationResult.base45Decoded = false
        return try {
            decoder.decode(input).also {
                verificationResult.base45Decoded = true
            }
        } catch (e: Throwable) {
            input.toByteArray()
        }
    }
}