package dgca.verifier.app.android.chain

import java.security.cert.Certificate

interface CertificateRepository {

    fun loadCertificate(kid: String): Certificate

}