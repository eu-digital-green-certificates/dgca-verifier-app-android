package dgca.verifier.app.decoder.chain

import COSE.HeaderKeys
import COSE.KeyKeys
import COSE.OneKey
import com.upokecenter.cbor.CBORObject
import java.math.BigInteger
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

class VerificationCryptoService(private val repository: CertificateRepository) : CryptoService {

    override fun getCborHeaders() = listOf<Pair<HeaderKeys, CBORObject>>()

    override fun getCborSigningKey() = OneKey(CBORObject.NewMap())

    override fun getCborVerificationKey(kid: String): OneKey {
        val certificate = getCertificate(kid)
        return when (certificate.publicKey) {
            is ECPublicKey -> buildEcKey(certificate.publicKey as ECPublicKey)
            else -> buildRsaKey(certificate.publicKey as RSAPublicKey)
        }
    }

    private fun buildRsaKey(rsaPublicKey: RSAPublicKey): OneKey {
        return OneKey(CBORObject.NewMap().also {
            it[KeyKeys.KeyType.AsCBOR()] = KeyKeys.KeyType_RSA
            it[KeyKeys.RSA_N.AsCBOR()] = stripLeadingZero(rsaPublicKey.modulus)
            it[KeyKeys.RSA_E.AsCBOR()] = stripLeadingZero(rsaPublicKey.publicExponent)
        })
    }

    private fun buildEcKey(publicKey: ECPublicKey): OneKey {
        return OneKey(CBORObject.NewMap().also {
            it[KeyKeys.KeyType.AsCBOR()] = KeyKeys.KeyType_EC2
            it[KeyKeys.EC2_Curve.AsCBOR()] = KeyKeys.EC2_P256
            it[KeyKeys.EC2_X.AsCBOR()] = stripLeadingZero(publicKey.w.affineX)
            it[KeyKeys.EC2_Y.AsCBOR()] = stripLeadingZero(publicKey.w.affineY)
        })
    }

    override fun getCertificate(kid: String) = repository.loadCertificate(kid)

    // Java's BigInteger adds a leading sign bit
    private fun stripLeadingZero(bigInteger: BigInteger): CBORObject {
        val bytes = bigInteger.toByteArray()
        return when {
            bytes.size % 8 != 0 && bytes[0] == 0x00.toByte() -> CBORObject.FromObject(
                bytes.drop(1).toByteArray()
            )
            else -> CBORObject.FromObject(bytes)
        }
    }
}