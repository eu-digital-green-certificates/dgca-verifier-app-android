package dgca.verifier.app.decoder.chain.schema

import dgca.verifier.app.decoder.chain.model.VerificationResult

interface SchemaValidator {

    fun validate(cbor: ByteArray, verificationResult: VerificationResult): Boolean
}