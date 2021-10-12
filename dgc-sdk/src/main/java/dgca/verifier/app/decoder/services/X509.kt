package dgca.verifier.app.decoder.services

import dgca.verifier.app.decoder.model.CertificateType
import java.io.ByteArrayInputStream
import java.security.cert.*


class X509 {
    private val OID_TEST = "1.3.6.1.4.1.1847.2021.1.1"
    private val OID_ALT_TEST = "1.3.6.1.4.1.0.1847.2021.1.1"
    private val OID_VACCINATION = "1.3.6.1.4.1.1847.2021.1.2"
    private val OID_ALT_VACCINATION = "1.3.6.1.4.1.0.1847.2021.1.2"
    private val OID_RECOVERY = "1.3.6.1.4.1.1847.2021.1.3"
    private val OID_ALT_RECOVERY = "1.3.6.1.4.1.0.1847.2021.1.3"

    fun checkIsSuitable(cert: String?, certType: CertificateType?): Boolean {
        val b64: ByteArray = org.bouncycastle.util.encoders.Base64.decode(cert)
        return isSuitable(b64, certType)
    }

    fun isSuitable(data: ByteArray?, certificateType: CertificateType?): Boolean {
        try {
            val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
            val cert: Certificate = cf.generateCertificate(ByteArrayInputStream(data))
            if (isType(cert as X509Certificate)) {
                val extendedKeys = cert.extendedKeyUsage
                return when (certificateType) {
                    CertificateType.TEST -> extendedKeys.contains(OID_TEST) || extendedKeys.contains(
                        OID_ALT_TEST
                    )
                    CertificateType.VACCINATION -> extendedKeys.contains(OID_VACCINATION) || extendedKeys.contains(
                        OID_ALT_VACCINATION
                    )
                    CertificateType.RECOVERY -> extendedKeys.contains(OID_RECOVERY) || extendedKeys.contains(
                        OID_ALT_RECOVERY
                    )
                    CertificateType.UNKNOWN -> false
                    else -> false
                }
            }
        } catch (e: CertificateException) {
            return false
        }
        return true
    }

    private fun isType(certificate: X509Certificate): Boolean {
        return try {
            val extendedKeyUsage: List<String> = certificate.extendedKeyUsage ?: return false

            extendedKeyUsage.contains(OID_TEST)
                    || extendedKeyUsage.contains(OID_ALT_TEST)
                    || extendedKeyUsage.contains(OID_RECOVERY)
                    || extendedKeyUsage.contains(OID_ALT_RECOVERY)
                    || extendedKeyUsage.contains(OID_VACCINATION)
                    || extendedKeyUsage.contains(OID_ALT_VACCINATION)
        } catch (e: CertificateParsingException) {
            false
        }
    }
}