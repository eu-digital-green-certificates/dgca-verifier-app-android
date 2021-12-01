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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import it.ministerodellasalute.verificaC19.ui.FirstActivity
import it.ministerodellasalute.verificaC19.ui.compounds.QuestionCompound
import it.ministerodellasalute.verificaC19.ui.main.MainActivity
import it.ministerodellasalute.verificaC19sdk.VerificaMinSDKVersionException
import it.ministerodellasalute.verificaC19sdk.VerificaMinVersionException
import it.ministerodellasalute.verificaC19sdk.model.CertificateSimple
import it.ministerodellasalute.verificaC19sdk.model.CertificateStatus
import it.ministerodellasalute.verificaC19sdk.model.SimplePersonModel
import it.ministerodellasalute.verificaC19sdk.model.VerificationViewModel
import it.ministerodellasalute.verificaC19sdk.util.FORMATTED_BIRTHDAY_DATE
import it.ministerodellasalute.verificaC19sdk.util.FORMATTED_VALIDATION_DATE
import it.ministerodellasalute.verificaC19sdk.util.TimeUtility.parseFromTo
import it.ministerodellasalute.verificaC19sdk.util.TimeUtility.parseTo
import it.ministerodellasalute.verificaC19sdk.util.YEAR_MONTH_DAY
import java.util.*

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class VerificationFragment : Fragment(), View.OnClickListener {

    private val args by navArgs<VerificationFragmentArgs>()
    private val viewModel by viewModels<VerificationViewModel>()

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var certificateModel: CertificateSimple

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
        viewModel.certificate.observe(viewLifecycleOwner) { certificate ->
            certificate?.let {
                certificateModel = it
                setPersonData(it.person, it.dateOfBirth)
                setupCertStatusView(it)
                setupTimeStamp(it)
                if (viewModel.getTotemMode() && (certificate.certificateStatus == CertificateStatus.VALID
                    || certificate.certificateStatus == CertificateStatus.PARTIALLY_VALID)) {
                    Handler().postDelayed({
                        activity?.onBackPressed()
                    }, 5000)
                }
            }
        }
        viewModel.inProgress.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
        try {
            viewModel.init(args.qrCodeText, true)
        }
        catch (e: VerificaMinSDKVersionException)
        {
            Log.d("VerificationFragment", "Min SDK Version Exception")
            createForceUpdateDialog()
        }
        catch (e: VerificaMinVersionException)
        {
            Log.d("VerificationFragment", "Min App Version Exception")
            createForceUpdateDialog()
        }

    }

    private fun setupCertStatusView(cert: CertificateSimple) {
        //val certStatus = viewModel.getCertificateStatus(cert)
        val certStatus = cert.certificateStatus
        if (certStatus !=null) {
            setBackgroundColor(certStatus)
            setPersonDetailsVisibility(certStatus)
            setValidationIcon(certStatus)
            setValidationMainText(certStatus)
            setScanModeText()
            setValidationSubTextVisibility(certStatus)
            setValidationSubText(certStatus)
            setLinkViews(certStatus)
        }
    }

    private fun setScanModeText() {
        val chosenScanMode = if (viewModel.getScanMode() == "3G") {
            getString(R.string.scan_mode_3G_header).substringAfter(' ').toUpperCase(Locale.ROOT)
        } else {
           getString(R.string.scan_mode_2G_header).substringAfter(' ').toUpperCase(Locale.ROOT)
        }
        val scanModeLabel = getString(R.string.label_scan_mode_ver)
        binding.scanModeText.text = getString(R.string.label_verification_scan_mode, scanModeLabel, chosenScanMode)
    }

    private fun setupTimeStamp(cert: CertificateSimple) {
        binding.validationDate.text = getString(
            R.string.label_validation_timestamp, cert.timeStamp?.parseTo(
                FORMATTED_VALIDATION_DATE
            )
        )
        binding.validationDate.visibility =View.VISIBLE
    }

    private fun setLinkViews(certStatus: CertificateStatus) {
        val questionMap: Map<String, String> = when (certStatus) {
            CertificateStatus.VALID, CertificateStatus.PARTIALLY_VALID -> mapOf(getString(R.string.label_what_can_be_done) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_VALID_YET -> mapOf(getString(R.string.label_when_qr_valid) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_VALID -> mapOf(getString(R.string.label_why_qr_not_valid) to "https://www.dgc.gov.it/web/faq.html#verifica19")
            CertificateStatus.NOT_EU_DCC -> mapOf(getString(R.string.label_which_qr_scan) to "https://www.dgc.gov.it/web/faq.html#verifica19")
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
            CertificateStatus.NOT_EU_DCC -> View.GONE
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
            CertificateStatus.NOT_EU_DCC -> getString(R.string.certificateNotDCC)
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
                    CertificateStatus.PARTIALLY_VALID -> R.drawable.ic_valid_cert
                    CertificateStatus.NOT_EU_DCC -> R.drawable.ic_technical_error
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
                    CertificateStatus.PARTIALLY_VALID -> R.color.green
                    else -> R.color.red_bg
                }
            )
        )
    }

    private fun setPersonData(person: SimplePersonModel?, dateOfBirth: String?) {
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

    private fun createForceUpdateDialog() {
        val builder = this.activity?.let { AlertDialog.Builder(requireContext()) }
        builder!!.setTitle(getString(R.string.updateTitle))
        builder!!.setMessage(getString(R.string.updateMessage))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            findNavController().popBackStack()
        }
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()
    }

}
