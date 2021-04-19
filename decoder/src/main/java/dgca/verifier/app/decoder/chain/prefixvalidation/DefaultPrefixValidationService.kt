package dgca.verifier.app.decoder.chain.prefixvalidation

import dgca.verifier.app.decoder.chain.model.VerificationResult

/**
 * Appends/drops a country-specific prefix from contents, e.g. "HC1"
 */
class DefaultPrefixValidationService(private val prefix: String = "HC1") : PrefixValidationService {

    override fun decode(input: String, verificationResult: VerificationResult): String = when {
        input.startsWith(prefix) -> input.drop(prefix.length)
            .also { verificationResult.valSuitePrefix = prefix }
        else -> input.also { verificationResult.valSuitePrefix = null }
    }
}