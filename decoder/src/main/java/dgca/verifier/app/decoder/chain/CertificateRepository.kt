package dgca.verifier.app.decoder.chain

import java.security.cert.Certificate

interface CertificateRepository {

    fun loadCertificate(kid: ByteArray): Certificate
}