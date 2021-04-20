package dgca.verifier.app.decoder

import android.annotation.SuppressLint
import dgca.verifier.app.decoder.chain.RemoteCachedCertificateRepository
import dgca.verifier.app.decoder.chain.VerificationCryptoService
import dgca.verifier.app.decoder.chain.base45.DefaultBase45Service
import dgca.verifier.app.decoder.chain.cbor.DefaultCborService
import dgca.verifier.app.decoder.chain.compression.DefaultCompressorService
import dgca.verifier.app.decoder.chain.cose.DefaultCoseService
import dgca.verifier.app.decoder.chain.model.VaccinationData
import dgca.verifier.app.decoder.chain.model.VerificationResult
import dgca.verifier.app.decoder.chain.prefixvalidation.DefaultPrefixValidationService
import org.junit.Test

class CoseValidationTest {

    var barcode =
        "HC1:NCFY70R30FFWTWGSLKC 4O992\$V M63TMF2V*D9LPC.3EHPCGEC27B72VF/347O4-M6Y9M6FOYG4ILDEI8GR3ZI$15MABL:E9CVBGEEWRMLE C39S0/ANZ52T82Z-73D63P1U 1\$PKC 72H2XX09WDH889V5"

    val trustJson = """
    [
      {
        \"kid\" : \"DEFBBA3378B322F5\",
        \"coord\" : [
          \"230ca0433313f4ef14ec0ab0477b241781d135ee09369507fcf44ca988ed09d6\",
          \"bf1bfe3d2bda606c841242b59c568d00e5c8dd114d223b2f5036d8c5bc68bf5d\"
        ]
      },
      {
        \"kid\" : \"FFFBBA3378B322F5\",
        \"coord\" : [
          \"9999a0433313f4ef14ec0ab0477b241781d135ee09369507fcf44ca988ed09d6\",
          \"9999fe3d2bda606c841242b59c568d00e5c8dd114d223b2f5036d8c5bc68bf5d\"
        ]
      }
    ]
    """

    // TODO:

    @Test
    fun validationTest() {
        decode(barcode)
    }

    @SuppressLint("SetTextI18n")
    fun decode(code: String) {
        val vaccinationData = VaccinationData()
        val verificationResult = VerificationResult()

        val repository = RemoteCachedCertificateRepository("https://dev.a-sit.at/certservice/cert")
        val cryptoService = VerificationCryptoService(repository)

        val plainInput = DefaultPrefixValidationService().decode(code, verificationResult)
        val compressedCose = DefaultBase45Service().decode(plainInput, verificationResult)
        val cose = DefaultCompressorService().decode(compressedCose, verificationResult)
        val cbor = DefaultCoseService(cryptoService).decode(cose, verificationResult)
        val result = DefaultCborService().decode(cbor, verificationResult)

    }

//    class MockRemoteCachedCertificateRepository(private val baseUrl: String) : CertificateRepository {
//
//        private val map = mutableMapOf<String, Certificate>()
//
//        override fun loadCertificate(kid: ByteArray): Certificate {
//            if (map.containsKey(kid)) return map[kid]!!
//            val request = Request.Builder().get().url("$baseUrl/$kid").build()
//            val response = OkHttpClient.Builder().build().newCall(request).execute()
//            response.body?.let {
//                val certificate =
//                    CertificateFactory.getInstance("X.509").generateCertificate(it.byteStream())
//                addCertificate(kid, certificate)
//                return certificate
//            }
//            throw IllegalArgumentException("Unable to get certificate for $kid at $baseUrl")
//        }
//
//        private fun addCertificate(kid: String, certificate: Certificate) {
//            map[kid] = certificate
//        }
//    }
}