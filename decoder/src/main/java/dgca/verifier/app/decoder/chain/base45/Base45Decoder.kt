package dgca.verifier.app.decoder.chain.base45

import java.math.BigInteger

/**
 *  The Base45 Data Decoding
 *
 *  https://datatracker.ietf.org/doc/draft-faltstrom-base45/?include_text=1
 */
@ExperimentalUnsignedTypes
class Base45Decoder {

    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
    private val int45 = BigInteger.valueOf(45)

    fun decode(input: String) =
        input.chunked(3).map(this::decodeThreeCharsPadded)
            .flatten().map { it.toByte() }.toByteArray()

    private fun decodeThreeCharsPadded(input: String): List<UByte> {
        val result = decodeThreeChars(input).toMutableList()
        when (input.length) {
            3 -> while (result.size < 2) result += 0U
        }
        return result.reversed()
    }

    private fun decodeThreeChars(list: String) =
        generateSequenceByDivRem(fromThreeCharValue(list))
            .map { it.toUByte() }.toList()

    private fun fromThreeCharValue(list: String): Long {
        return list.foldIndexed(0L, { index, acc: Long, element ->
            if (!alphabet.contains(element)) throw IllegalArgumentException()
            pow(int45, index) * alphabet.indexOf(element) + acc
        })
    }

    private fun generateSequenceByDivRem(seed: Long) =
        generateSequence(seed) { if (it >= 256) it.div(256) else null }
            .map { it.rem(256).toInt() }

    private fun pow(base: BigInteger, exp: Int) = base.pow(exp).toLong()
}
