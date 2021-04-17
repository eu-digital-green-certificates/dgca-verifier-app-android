package dgca.verifier.app.android.chain

/**
 * Appends/drops a country-specific prefix from contents, e.g. "HC1"
 */
class DefaultValSuiteService(private val prefix: String = "HC1") : ValSuiteService {

    override fun encode(input: String): String {
        return "$prefix$input";
    }

    override fun decode(input: String, verificationResult: VerificationResult): String = when {
        input.startsWith(prefix) -> input.drop(prefix.length).also { verificationResult.valSuitePrefix = prefix }
        else -> input.also { verificationResult.valSuitePrefix = null }
    }
}