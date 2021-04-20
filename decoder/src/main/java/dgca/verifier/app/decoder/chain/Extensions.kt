package dgca.verifier.app.decoder.chain

import java.util.Base64

fun ByteArray.asBase64() = Base64.getEncoder().encodeToString(this)

fun ByteArray.toBase64(): String = Base64.getUrlEncoder().encodeToString(this)

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun String.fromBase64() = Base64.getDecoder().decode(this)

