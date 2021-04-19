package dgca.verifier.app.decoder.chain.compression

import dgca.verifier.app.decoder.chain.model.VerificationResult
import java.util.zip.InflaterInputStream

class DefaultCompressorService : CompressorService {

    /**
     * *Optionally* decompresses input with ZLIB = inflating.
     *
     * If the [input] does not start with ZLIB magic numbers (0x78), no decompression happens
     */
    override fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray {
        verificationResult.zlibDecoded = false
        if (input.size >= 2 && input[0] == 0x78.toByte()) { // ZLIB magic headers
            if (input[1] == 0x01.toByte() || // Level 1
                input[1] == 0x5E.toByte() || // Level 2 - 5
                input[1] == 0x9C.toByte() || // Level 6
                input[1] == 0xDA.toByte()    // Level 7 - 9
            ) {
                return try {
                    InflaterInputStream(input.inputStream()).readBytes().also {
                        verificationResult.zlibDecoded = true
                    }
                } catch (e: Throwable) {
                    input
                }
            }
        }
        return input
    }
}