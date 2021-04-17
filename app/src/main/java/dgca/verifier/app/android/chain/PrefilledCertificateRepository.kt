package dgca.verifier.app.android.chain

import java.security.cert.Certificate

class PrefilledCertificateRepository : CertificateRepository {

    private val map = mutableMapOf<String, Certificate>()

    override fun loadCertificate(kid: String): Certificate {
        if (map.containsKey(kid)) return map[kid]!!
        throw IllegalArgumentException("kid not known: $kid")
    }

    fun addCertificate(kid: String, certificate: Certificate) {
        map[kid] = certificate
    }
}


