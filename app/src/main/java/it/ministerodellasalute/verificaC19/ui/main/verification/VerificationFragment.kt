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
 */

package it.ministerodellasalute.verificaC19.ui.main.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import it.ministerodellasalute.verificaC19.*
import it.ministerodellasalute.verificaC19.databinding.FragmentVerificationBinding
import it.ministerodellasalute.verificaC19.model.CertificateModel
import it.ministerodellasalute.verificaC19.model.CertificateStatus
import it.ministerodellasalute.verificaC19.model.PersonModel
import it.ministerodellasalute.verificaC19.ui.compounds.QuestionCompound
import java.util.*

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationFragment : Fragment(), View.OnClickListener {

    private val args by navArgs<VerificationFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var certificateModel: CertificateModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener(this)
        binding.validationDate.text = getString(
            R.string.label_validation_timestamp, Date().time.parseTo(
                FORMATTED_VALIDATION_DATE
            )
        )
        viewModel.certificate.observe(viewLifecycleOwner) { certificate ->
            certificate?.let {
                certificateModel = it
                setPersonData(it.person, it.dateOfBirth)
                setupCertStatusView(it)
            }
        }
        viewModel.inProgress.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
        viewModel.init(args.qrCodeText)
    }

    private fun setupCertStatusView(cert: CertificateModel) {
        val certStatus = viewModel.getCertificateStatus(cert)
        setBackgroundColor(certStatus)
        setPersonDetailsVisibility(certStatus)
        setValidationIcon(certStatus)
        setValidationMainText(certStatus)
        setValidationSubTextVisibility(certStatus)
        setValidationSubText(certStatus)
        setLinkViews(certStatus)
    }

    private fun setLinkViews(certStatus: CertificateStatus) {
        val questionMap: Map<String, String> = when (certStatus) {
            CertificateStatus.VALID, CertificateStatus.PARTIALLY_VALID -> mapOf(getString(R.string.label_what_can_be_done) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_VALID_YET -> mapOf(getString(R.string.label_when_qr_valid) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_VALID -> mapOf(getString(R.string.label_why_qr_not_valid) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_GREEN_PASS -> mapOf(getString(R.string.label_which_qr_scan) to "https://www.dgc.gov.it/web/faq.html#verifica19")
        }
        questionMap.map {
            val compound = QuestionCompound(context)
            compound.setupWithLabels(it.key, it.value)
            binding.questionContainer.addView(compound)
        }
        binding.questionContainer.clipChildren = false
    }

    private fun setValidationSubTextVisibility(certStatus: CertificateStatus) {
        binding.subtitleText.visibility = when (certStatus) {
            CertificateStatus.NOT_GREEN_PASS -> View.GONE
            else -> View.VISIBLE
        }
    }

    private fun setValidationSubText(certStatus: CertificateStatus) {
        binding.subtitleText.text =
            when (certStatus) {
                CertificateStatus.VALID, CertificateStatus.PARTIALLY_VALID -> getString(R.string.subtitle_text)
                CertificateStatus.NOT_VALID, CertificateStatus.NOT_VALID_YET -> getString(R.string.subtitle_text_notvalid)
                else -> getString(R.string.subtitle_text_technicalError)
            }
    }

    private fun setValidationMainText(certStatus: CertificateStatus) {
        binding.certificateValid.text = when (certStatus) {
            CertificateStatus.VALID -> getString(R.string.certificateValid)
            CertificateStatus.PARTIALLY_VALID -> getString(R.string.certificatePartiallyValid)
            CertificateStatus.NOT_GREEN_PASS -> getString(R.string.certificateNotDCC)
            CertificateStatus.NOT_VALID -> getString(R.string.certificateNonValid)
            CertificateStatus.NOT_VALID_YET -> getString(R.string.certificateNonValidYet)
        }
    }

    private fun setValidationIcon(certStatus: CertificateStatus) {
        binding.checkmark.background =
            ContextCompat.getDrawable(
                requireContext(), when (certStatus) {
                    CertificateStatus.VALID -> R.drawable.ic_valid_cert
                    CertificateStatus.NOT_VALID_YET -> R.drawable.ic_not_valid_yet
                    CertificateStatus.PARTIALLY_VALID -> R.drawable.ic_locally_valid
                    CertificateStatus.NOT_GREEN_PASS -> R.drawable.ic_technical_error
                    else -> R.drawable.ic_invalid
                }
            )
    }

    private fun setPersonDetailsVisibility(certStatus: CertificateStatus) {
        binding.containerPersonDetails.visibility = when (certStatus) {
            CertificateStatus.VALID, CertificateStatus.NOT_VALID, CertificateStatus.NOT_VALID_YET, CertificateStatus.PARTIALLY_VALID -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setBackgroundColor(certStatus: CertificateStatus) {
        binding.verificationBackground.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                when (certStatus) {
                    CertificateStatus.VALID -> R.color.green
                    CertificateStatus.PARTIALLY_VALID -> R.color.blue_bg
                    else -> R.color.red_bg
                }
            )
        )
    }

    private fun setPersonData(person: PersonModel?, dateOfBirth: String?) {
        binding.nameStandardisedText.text = person?.familyName.plus(" ").plus(person?.givenName)
        binding.birthdateText.text =
            dateOfBirth?.parseFromTo(YEAR_MONTH_DAY, FORMATTED_BIRTHDAY_DATE) ?: ""
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_button -> findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
