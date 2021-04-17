package dgca.verifier.app.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dgca.verifier.app.android.chain.CborProcessingChain
import dgca.verifier.app.android.chain.DefaultBase45Service
import dgca.verifier.app.android.chain.DefaultCborService
import dgca.verifier.app.android.chain.DefaultCompressorService
import dgca.verifier.app.android.chain.DefaultCoseService
import dgca.verifier.app.android.chain.DefaultValSuiteService
import dgca.verifier.app.android.chain.RemoteCachedCertificateRepository
import dgca.verifier.app.android.chain.VaccinationData
import dgca.verifier.app.android.chain.VerificationCryptoService
import dgca.verifier.app.android.chain.VerificationResult
import dgca.verifier.app.android.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
@ExperimentalUnsignedTypes
class ResultFragment : Fragment() {

    private val args by navArgs<ResultFragmentArgs>()
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
            val vaccinationData: VaccinationData
            val decodingChain = buildChain()
            val verificationResult = VerificationResult()
            vaccinationData = decodingChain.verify(code, verificationResult)

            withContext(Dispatchers.Main) {
                binding.result.text = "$code\n  Decoded: \n$vaccinationData \n$verificationResult"
            }
        }
    }

    private fun buildChain(): CborProcessingChain {
        val repository = RemoteCachedCertificateRepository("https://dev.a-sit.at/certservice/cert")
        val cryptoService = VerificationCryptoService(repository)

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