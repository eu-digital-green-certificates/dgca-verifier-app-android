/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by mykhailo.nester on 4/24/21 2:10 PM
 */

package dgca.verifier.app.android.verification

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.FragmentResultBinding
import dgca.verifier.app.decoder.chain.model.GreenCertificate
import dgca.verifier.app.decoder.chain.model.IdentifierType

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationFragment : Fragment() {

    private val args by navArgs<VerificationFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = CertListAdapter(layoutInflater)
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
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel.init(args.qrCodeText)

        viewModel.verificationResult.observe(viewLifecycleOwner, {
            if (it.isValid()) {
                binding.status.text = getString(R.string.cert_valid)
                binding.status.setTextColor(Color.GREEN)
                binding.certStatusIcon.setImageResource(R.drawable.ic_baseline_check_24)
            } else {
                binding.status.text = getString(R.string.cert_invalid)
                binding.status.setTextColor(Color.RED)
                binding.certStatusIcon.setImageResource(R.drawable.ic_baseline_close_24)
            }
        })
        viewModel.certificate.observe(viewLifecycleOwner, { certificate ->
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
            }
        })
        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.progressBar.isVisible = it
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCertType(certificate: GreenCertificate): String {
        return when {
            certificate.vaccinations.isNotEmpty() -> getString(R.string.type_vaccination)
            certificate.recoveryStatements.isNotEmpty() -> getString(R.string.type_recovered)
            certificate.tests.isNotEmpty() -> getString(R.string.type_test)
            else -> ""
        }
    }
}