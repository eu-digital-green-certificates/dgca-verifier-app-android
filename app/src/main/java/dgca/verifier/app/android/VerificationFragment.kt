package dgca.verifier.app.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.databinding.FragmentResultBinding

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationFragment : Fragment() {

    private val args by navArgs<VerificationFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

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
        viewModel.init(args.qrCodeText)

        viewModel.verificationResult.observe(viewLifecycleOwner, {
            // TODO: display data
        })
        viewModel.certificate.observe(viewLifecycleOwner, {
            // TODO: display data
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}