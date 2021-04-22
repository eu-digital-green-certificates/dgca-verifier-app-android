package dgca.verifier.app.decoder.chain.cbor

import dgca.verifier.app.decoder.chain.model.GreenCertificate
import dgca.verifier.app.decoder.chain.model.VerificationResult

interface CborService {

    fun decode(input: ByteArray, verificationResult: VerificationResult): GreenCertificate?
}