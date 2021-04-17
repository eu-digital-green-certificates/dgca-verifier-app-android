package dgca.verifier.app.android.chain

interface Base45Service {

    fun encode(input: ByteArray): String

    fun decode(input: String, verificationResult: VerificationResult): ByteArray
}