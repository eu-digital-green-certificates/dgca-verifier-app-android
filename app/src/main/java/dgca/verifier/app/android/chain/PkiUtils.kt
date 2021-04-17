package dgca.verifier.app.android.chain

import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Random

class PkiUtils {

    fun selfSignCertificate(subjectName: X500Name, keyPair: KeyPair): X509Certificate {
        val subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(keyPair.public.encoded))
        val keyUsage = KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment)
        val keyUsageExt = Extension.create(Extension.keyUsage, true, keyUsage)
        val notBefore = Instant.now()
        val notAfter = notBefore.plus(30, ChronoUnit.DAYS)
        val serialNumber = BigInteger(32, Random()).abs()
        val builder = X509v3CertificateBuilder(
            subjectName, serialNumber, Date.from(notBefore), Date.from(notAfter), subjectName, subjectPublicKeyInfo
        )
        listOf(keyUsageExt).forEach<Extension> { builder.addExtension(it) }
        val contentSigner = JcaContentSignerBuilder(getAlgorithm(keyPair.private)).build(keyPair.private)
        val certificateHolder = builder.build(contentSigner)
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(certificateHolder.encoded)) as X509Certificate
    }

    private fun getAlgorithm(private: PrivateKey) = when (private) {
        is ECPrivateKey -> "SHA256withECDSA"
        else -> "SHA256withRSA"
    }
}


