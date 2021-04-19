package dgca.verifier.app.decoder.chain

import java.util.*

fun ByteArray.asBase64() = Base64.getEncoder().encodeToString(this)

fun ByteArray.asBase64Url() = Base64.getUrlEncoder().encodeToString(this)

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun String.fromBase64() = Base64.getDecoder().decode(this)

