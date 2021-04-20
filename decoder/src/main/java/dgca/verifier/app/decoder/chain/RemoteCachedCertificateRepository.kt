package dgca.verifier.app.decoder.chain

import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

class RemoteCachedCertificateRepository(private val baseUrl: String) : CertificateRepository {

    private val map = mutableMapOf<String, Certificate>()

    override fun loadCertificate(kid: ByteArray): Certificate {
        val key = kid.toBase64()
        if (map.containsKey(key)) return map[key]!!
        val request = Request.Builder().get().url("$baseUrl/$key").build()
        val response = OkHttpClient.Builder().build().newCall(request).execute()
        response.body?.let {
            val certificate =
                CertificateFactory.getInstance("X.509").generateCertificate(it.byteStream())
            map[key] = certificate

            return certificate
        }
        throw IllegalArgumentException("Unable to get certificate for $kid at $baseUrl")
    }

    private fun addCertificate(kid: ByteArray, certificate: Certificate) {
        val key = kid.toBase64()
        map[key] = certificate
    }
}
