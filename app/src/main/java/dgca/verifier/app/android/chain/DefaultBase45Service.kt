package dgca.verifier.app.android.chain

@ExperimentalUnsignedTypes
class DefaultBase45Service : Base45Service {

    private val encoder = Base45Encoder()

    override fun encode(input: ByteArray) =
        encoder.encode(input)

    override fun decode(input: String, verificationResult: VerificationResult): ByteArray {
        verificationResult.base45Decoded = false
        return try {
            encoder.decode(input).also {
                verificationResult.base45Decoded = true
            }
        } catch (e: Throwable) {
            input.toByteArray()
        }
    }
}