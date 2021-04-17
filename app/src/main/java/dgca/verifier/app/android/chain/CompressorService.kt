package dgca.verifier.app.android.chain

interface CompressorService {

    fun encode(input: ByteArray): ByteArray

    fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray

}