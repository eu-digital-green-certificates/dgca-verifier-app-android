package dgca.verifier.app.android.chain

class CborProcessingChain(
    private val cborService: CborService,
    private val coseService: CoseService,
    private val valSuiteService: ValSuiteService,
    private val compressorService: CompressorService,
    private val base45Service: Base45Service
) {

    fun process(input: VaccinationData): ResultCbor {
        val cbor = cborService.encode(input)
        val cose = coseService.encode(cbor)
        val comCose = compressorService.encode(cose)
        val encodedComCose = base45Service.encode(comCose)
        val prefEncodedComCose = valSuiteService.encode(encodedComCose)
        return ResultCbor(cbor, cose, comCose, prefEncodedComCose)
    }

    fun verify(input: String, verificationResult: VerificationResult = VerificationResult()): VaccinationData {
        val plainInput = valSuiteService.decode(input, verificationResult)
        val compressedCose = base45Service.decode(plainInput, verificationResult)
        val cose = compressorService.decode(compressedCose, verificationResult)
        val cbor = coseService.decode(cose, verificationResult)
        return cborService.decode(cbor, verificationResult)
    }

}

class VerificationResult {
    var base45Decoded = false
    var valSuitePrefix: String? = null
    var zlibDecoded = false
    var coseVerified = false
    var cborDecoded = false
}

data class ResultCbor(
    val cbor: ByteArray,
    val cose: ByteArray,
    val compressedCose: ByteArray,
    val prefixedEncodedCompressedCose: String
)
