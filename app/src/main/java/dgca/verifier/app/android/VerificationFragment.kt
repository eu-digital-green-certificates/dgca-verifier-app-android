package dgca.verifier.app.android

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
import dgca.verifier.app.decoder.chain.model.IdentifierType
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
    private lateinit var adapter: MediaListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = MediaListAdapter(layoutInflater)
    }

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

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun decode(code: String) {
        GlobalScope.launch {
            val certificate: GreenCertificate?
            val decodingChain = buildChain()
            val verificationResult = VerificationResult()
            certificate = decodingChain.verify(code, verificationResult)

            withContext(Dispatchers.Main) {
                if (certificate != null) {
                    adapter.update(certificate.vaccinations)
                    binding.personFullName.text = certificate.subject.givenName + "\n" + certificate.subject.familyName
                    binding.type.text = getCertType(certificate)

                    val personalInfo = StringBuilder()
                    val identifier = certificate.subject.identifiers?.first()
                    when (identifier?.type) {
                        IdentifierType.PASSPORT -> personalInfo.append("Passport: ${identifier.id}")
                        IdentifierType.NATIONAL_IDENTIFIER -> { // TODO: update
                        }
                        IdentifierType.CITIZENSHIP -> { // TODO: update
                        }
                        IdentifierType.HEALTH -> { // TODO: update
                        }
                        null -> {
                        }
                    }
                    personalInfo.append("\n")
                    personalInfo.append("Date of Birth: ${certificate.subject.dateOfBirth}")
                    binding.personInfo.text = personalInfo

                    if (isCertValid(verificationResult)) {
                        binding.status.text = getString(R.string.cert_valid)
                        binding.status.setTextColor(Color.GREEN)
                        binding.certStatusIcon.setImageResource(R.drawable.ic_baseline_check_24)
                    } else {
                        binding.status.text = getString(R.string.cert_invalid)
                        binding.status.setTextColor(Color.RED)
                        binding.certStatusIcon.setImageResource(R.drawable.ic_baseline_close_24)
                    }
                }
            }
        }
    }

    private fun isCertValid(result: VerificationResult): Boolean =
        result.base45Decoded && result.zlibDecoded && result.coseVerified && result.cborDecoded && result.isSchemaValid

    private fun getCertType(certificate: GreenCertificate): String {
        return when {
            certificate.vaccinations.isNotEmpty() -> getString(R.string.type_vaccination)
            certificate.recoveryStatements.isNotEmpty() -> getString(R.string.type_recovered)
            certificate.tests.isNotEmpty() -> getString(R.string.type_test)
            else -> ""
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