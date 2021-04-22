package dgca.verifier.app.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dgca.verifier.app.android.databinding.FragmentResultBinding
import dgca.verifier.app.decoder.chain.CborProcessingChain
import dgca.verifier.app.decoder.chain.RemoteCachedCertificateRepository
import dgca.verifier.app.decoder.chain.SchemaValidator
import dgca.verifier.app.decoder.chain.VerificationCryptoService
import dgca.verifier.app.decoder.chain.base45.DefaultBase45Service
import dgca.verifier.app.decoder.chain.cbor.DefaultCborService
import dgca.verifier.app.decoder.chain.compression.DefaultCompressorService
import dgca.verifier.app.decoder.chain.cose.DefaultCoseService
import dgca.verifier.app.decoder.chain.model.GreenCertificate
import dgca.verifier.app.decoder.chain.model.VerificationResult
import dgca.verifier.app.decoder.chain.prefixvalidation.DefaultPrefixValidationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BASE_URL = "https://dgc.a-sit.at/ehn/cert"

@ExperimentalUnsignedTypes
class VerificationFragment : Fragment() {

    private val args by navArgs<VerificationFragmentArgs>()
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decode(args.qrCodeText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    fun decode(code: String) {
        GlobalScope.launch {
            val vaccinationData: GreenCertificate?
            val decodingChain = buildChain()
            val verificationResult = VerificationResult()
            vaccinationData = decodingChain.verify(code, verificationResult)

            withContext(Dispatchers.Main) {
                binding.result.text = "$code\n  Decoded: \n$vaccinationData \n$verificationResult"
            }
        }
    }

    private fun buildChain(): CborProcessingChain {
        val repository = RemoteCachedCertificateRepository(BASE_URL)
        val cryptoService = VerificationCryptoService(repository)

        val coseService = DefaultCoseService(cryptoService)
        val valSuiteService = DefaultPrefixValidationService()
        val compressorService = DefaultCompressorService()
        val base45Service = DefaultBase45Service()
        val cborService = DefaultCborService()
        val schemaValidator = SchemaValidator()

        return CborProcessingChain(
            cborService,
            coseService,
            valSuiteService,
            compressorService,
            base45Service,
            schemaValidator
        )
    }
}