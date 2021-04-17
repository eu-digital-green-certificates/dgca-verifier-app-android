package dgca.verifier.app.android.chain

interface CoseService {

    fun encode(input: ByteArray): ByteArray

    fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray
}