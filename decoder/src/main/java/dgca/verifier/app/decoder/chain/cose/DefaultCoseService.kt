package dgca.verifier.app.decoder.chain.cose

import COSE.HeaderKeys
import COSE.MessageTag
import COSE.Sign1Message
import dgca.verifier.app.decoder.chain.CryptoService
import dgca.verifier.app.decoder.chain.model.VerificationResult

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

    private fun getKid(it: Sign1Message): ByteArray? {
        val key = HeaderKeys.KID.AsCBOR()
        if (it.protectedAttributes.ContainsKey(key)) {
            return it.protectedAttributes.get(key).GetByteString()
        } else if (it.unprotectedAttributes.ContainsKey(key)) {
            return it.unprotectedAttributes.get(key).GetByteString()
        }
        return null
    }
}