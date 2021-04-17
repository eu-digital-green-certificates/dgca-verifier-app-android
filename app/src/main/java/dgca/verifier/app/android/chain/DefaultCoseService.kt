package dgca.verifier.app.android.chain

import COSE.Attribute
import COSE.HeaderKeys
import COSE.MessageTag
import COSE.Sign1Message
import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType

class DefaultCoseService(private val cryptoService: CryptoService) : CoseService {

    override fun encode(input: ByteArray): ByteArray {
        return Sign1Message().also {
            it.SetContent(input)
            for (header in cryptoService.getCborHeaders()) {
                it.addAttribute(header.first, header.second, Attribute.PROTECTED)
            }
            it.sign(cryptoService.getCborSigningKey())
        }.EncodeToBytes()
    }

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