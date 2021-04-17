package dgca.verifier.app.android

import COSE.HeaderKeys
import dgca.verifier.app.android.chain.CborProcessingChain
import dgca.verifier.app.android.chain.CryptoService
import dgca.verifier.app.android.chain.DefaultBase45Service
import dgca.verifier.app.android.chain.DefaultCborService
import dgca.verifier.app.android.chain.DefaultCompressorService
import dgca.verifier.app.android.chain.DefaultCoseService
import dgca.verifier.app.android.chain.DefaultValSuiteService
import dgca.verifier.app.android.chain.PrefilledCertificateRepository
import dgca.verifier.app.android.chain.RandomEcKeyCryptoService
import dgca.verifier.app.android.chain.SampleData
import dgca.verifier.app.android.chain.VaccinationData
import dgca.verifier.app.android.chain.VerificationCryptoService
import dgca.verifier.app.android.chain.VerificationResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
class CborProcessingChainTest {

    @Test
    fun pastInfected() {
        verify(SampleData.recovery, RandomEcKeyCryptoService())
    }

    @Test
    fun tested() {
        verify(SampleData.test, RandomEcKeyCryptoService())
    }

    @Test
    fun vaccinated() {
        verify(SampleData.vaccination, RandomEcKeyCryptoService())
    }

    private fun verify(jsonInput: String, cryptoService: CryptoService) {
        val input = Json { isLenient = true }.decodeFromString<VaccinationData>(jsonInput)
        val verificationResult = VerificationResult()

        val encodingChain = buildChain(cryptoService)
        val kid =
            cryptoService.getCborHeaders()
                .first { it.first.AsCBOR() == HeaderKeys.KID.AsCBOR() }.second.AsString()
        val certificate = cryptoService.getCertificate(kid)
        val certificateRepository = PrefilledCertificateRepository()
        certificateRepository.addCertificate(kid, certificate)
        val decodingChain = buildChain(VerificationCryptoService(certificateRepository))

        val output = encodingChain.process(input)

        val vaccinationData =
            decodingChain.verify(output.prefixedEncodedCompressedCose, verificationResult)
        assertThat(vaccinationData, equalTo(input))
        assertThat(verificationResult.cborDecoded, equalTo(true))
    }

    private fun buildChain(cryptoService: CryptoService): CborProcessingChain {
        val coseService = DefaultCoseService(cryptoService)
        val valSuiteService = DefaultValSuiteService()
        val compressorService = DefaultCompressorService()
        val base45Service = DefaultBase45Service()
        val cborService = DefaultCborService()

        return CborProcessingChain(
            cborService,
            coseService,
            valSuiteService,
            compressorService,
            base45Service
        )
    }
}
