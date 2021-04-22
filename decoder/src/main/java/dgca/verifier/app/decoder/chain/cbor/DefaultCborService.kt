package dgca.verifier.app.decoder.chain.cbor

import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.chain.cwt.CwtHeaderKeys
import dgca.verifier.app.decoder.chain.model.VaccinationData
import dgca.verifier.app.decoder.chain.model.VerificationResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.time.Instant

@ExperimentalSerializationApi
class DefaultCborService : CborService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): VaccinationData {
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

            return Cbor { ignoreUnknownKeys = true }.decodeFromByteArray<VaccinationData>(hcertv1)
                .also {
                    verificationResult.cborDecoded = true
                }
        } catch (e: Throwable) {
            return VaccinationData()
        }
    }
}