package dgca.verifier.app.decoder.chain.cose

import COSE.HeaderKeys
import COSE.MessageTag
import COSE.Sign1Message
import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType
import dgca.verifier.app.decoder.chain.CryptoService
import dgca.verifier.app.decoder.chain.model.VerificationResult
import dgca.verifier.app.decoder.chain.toHexString

class DefaultCoseService(private val cryptoService: CryptoService) : CoseService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray {
        verificationResult.coseVerified = false
        return try {
            (Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message).also {
                getKid(it)?.let { kid ->
                    try {
                        val verificationKey = cryptoService.getCborVerificationKey(kid)
                        verificationResult.coseVerified = it.validate(verificationKey)
                    } catch (e: Throwable) {
                        it.GetContent()
                    }
                }
            }.GetContent()
        } catch (e: Throwable) {
            input
        }
    }

    private fun getKid(it: Sign1Message): String? {
        val key = HeaderKeys.KID.AsCBOR()
        if (it.protectedAttributes.ContainsKey(key)) {
            return asString(it.protectedAttributes.get(key))
        } else if (it.unprotectedAttributes.ContainsKey(key)) {
            return asString(it.unprotectedAttributes.get(key))
        }
        return null
    }

    private fun asString(get: CBORObject): String? = when (get.type) {
        CBORType.ByteString -> get.GetByteString().toHexString()
        else -> get.AsString()
    }
}