package dgca.verifier.app.decoder.chain.prefixvalidation

import dgca.verifier.app.decoder.chain.model.VerificationResult

/**
 * Drops a country-specific prefix from contents, e.g. "HC1:o"
 */
class DefaultPrefixValidationService(private val prefix: String = "HC1:") : PrefixValidationService {

    override fun decode(input: String, verificationResult: VerificationResult): String = when {
        input.startsWith(prefix) -> input.drop(prefix.length).also { verificationResult.countryPrefix = prefix }
        else -> input.also { verificationResult.countryPrefix = null }
    }
}