package dgca.verifier.app.decoder.chain.cwt

import com.upokecenter.cbor.CBORObject

/**
 * Adapted from [COSE.HeaderKeys] to use CWT specific ones (https://tools.ietf.org/html/rfc8392)
 */
sealed class CwtHeaderKeys(value: Int) {

    private val value: CBORObject = CBORObject.FromObject(value)

    fun AsCBOR(): CBORObject {
        return value
    }

    object ISSUER : CwtHeaderKeys(1)
    object SUBJECT : CwtHeaderKeys(2)
    object AUDIENCE : CwtHeaderKeys(3)
    object EXPIRATION : CwtHeaderKeys(4)
    object NOT_BEFORE : CwtHeaderKeys(5)
    object ISSUED_AT : CwtHeaderKeys(6)
    object CWT_ID : CwtHeaderKeys(7)

    object HCERT : CwtHeaderKeys(-260)
}