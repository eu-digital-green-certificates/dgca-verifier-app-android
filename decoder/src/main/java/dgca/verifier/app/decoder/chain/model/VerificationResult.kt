package dgca.verifier.app.decoder.chain.model

data class VerificationResult(
    var base45Decoded: Boolean = false,
    var countryPrefix: String? = null,
    var zlibDecoded: Boolean = false,
    var coseVerified: Boolean = false,
    var cborDecoded: Boolean = false,
    var isSchemaValid: Boolean = false
) {

    override fun toString(): String {
        return "VerificationResult: \n" +
                "base45Decoded: $base45Decoded \n" +
                "valSuitePrefix: $countryPrefix \n" +
                "zlibDecoded: $zlibDecoded \n" +
                "coseVerified: $coseVerified \n" +
                "cborDecoded: $cborDecoded \n" +
                "isSchemaValid: $isSchemaValid"
    }
}