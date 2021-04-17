package dgca.verifier.app.android.chain

interface ValSuiteService {

    fun encode(input: String): String

    fun decode(input: String, verificationResult: VerificationResult): String
}