package dgca.verifier.app.decoder.chain

import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

class RemoteCachedCertificateRepository(private val baseUrl: String) : CertificateRepository {

    private val map = mutableMapOf<String, Certificate>()

    override fun loadCertificate(kid: String): Certificate {
        if (map.containsKey(kid)) return map[kid]!!
        val request = Request.Builder().get().url("$baseUrl/$kid").build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()
        response.body?.let {
            val certificate =
                CertificateFactory.getInstance("X.509").generateCertificate(it.byteStream())
            addCertificate(kid, certificate)
            return certificate
        }
        throw IllegalArgumentException("Unable to get certificate for $kid at $baseUrl")
    }

    private fun addCertificate(kid: String, certificate: Certificate) {
        map[kid] = certificate
    }
}
