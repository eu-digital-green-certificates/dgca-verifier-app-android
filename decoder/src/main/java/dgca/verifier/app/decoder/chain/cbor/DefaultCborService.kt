package dgca.verifier.app.decoder.chain.cbor

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.chain.cwt.CwtHeaderKeys
import dgca.verifier.app.decoder.chain.model.GreenCertificate
import dgca.verifier.app.decoder.chain.model.VerificationResult
import java.time.Instant

class DefaultCborService : CborService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): GreenCertificate? {
        verificationResult.cborDecoded = false
        try {
            val map = CBORObject.DecodeFromBytes(input)

            val issuer = map[CwtHeaderKeys.ISSUER.AsCBOR()].AsString()
            if (issuer != "AT") throw IllegalArgumentException("Issuer not correct: $issuer")

            val issuedAt = Instant.ofEpochSecond(map[CwtHeaderKeys.ISSUED_AT.AsCBOR()].AsInt64())
            if (issuedAt.isAfter(Instant.now())) throw IllegalArgumentException("IssuedAt not correct: $issuedAt")

            val expirationTime =
                Instant.ofEpochSecond(map[CwtHeaderKeys.EXPIRATION.AsCBOR()].AsInt64())
            if (expirationTime.isBefore(Instant.now())) throw IllegalArgumentException("Expiration not correct: $expirationTime")

            val hcert = map[CwtHeaderKeys.HCERT.AsCBOR()]
            val hcertv1 = hcert[CBORObject.FromObject(1)].EncodeToBytes()

            return CBORMapper()
                .readValue(hcertv1, GreenCertificate::class.java)
                .also { verificationResult.cborDecoded = true }

        } catch (e: Throwable) {
            return null
        }
    }
}