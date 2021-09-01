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
 *  Created by osarapulov on 9/1/21 7:24 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import dgca.verifier.app.android.FORMATTED_YEAR_MONTH_DAY
import dgca.verifier.app.android.R
import dgca.verifier.app.android.YEAR_MONTH_DAY
import dgca.verifier.app.android.databinding.ViewDetailedCertificateViewBinding
import dgca.verifier.app.android.model.CertificateModel
import dgca.verifier.app.android.parseFromTo
import dgca.verifier.app.android.verification.VerificationError

class DetailedCertificateView(context: Context, attrs: AttributeSet?) :
    MaterialCardView(context, attrs) {
    private val binding: ViewDetailedCertificateViewBinding =
        ViewDetailedCertificateViewBinding.inflate(LayoutInflater.from(context), this)
    private var isExpanded = false
    private lateinit var certificateModelAndVerificationError: Pair<CertificateModel, VerificationError?>

    init {
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            context.resources.getDimension(R.dimen.detailed_verification_result_banner_radius),
            context.resources.displayMetrics
        )
        strokeWidth = resources.getDimensionPixelSize(R.dimen.default_stroke_width)
        setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)))

        binding.expandButton.setOnClickListener {
            isExpanded = !isExpanded
            setExpanded(isExpanded)
            setUp(certificateModelAndVerificationError)
        }
    }

    private fun setExpanded(expanded: Boolean) {
        binding.expandableContent.visibility = if (expanded) View.VISIBLE else View.GONE
        binding.expandButton.setImageResource(if (expanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
    }

    fun setCertificateModel(
        certificateModel: CertificateModel,
        verificationError: VerificationError?
    ) {
        certificateModelAndVerificationError = Pair(certificateModel, verificationError)
        setUp(certificateModelAndVerificationError)
    }

    private fun setUp(certificateModelAndVerificationError: Pair<CertificateModel?, VerificationError?>) {
        setUpCertificateType(certificateModelAndVerificationError.first!!)
        setUpPossibleLimitation(certificateModelAndVerificationError.second)
        setUpDateOfBirth(certificateModelAndVerificationError.first!!)
    }

    private fun setUpCertificateType(certificateModel: CertificateModel) {
        binding.certificateTypeName.text = when {
            certificateModel.vaccinations?.isNotEmpty() == true -> context.getString(
                R.string.type_vaccination,
                certificateModel.vaccinations.first().doseNumber,
                certificateModel.vaccinations.first().totalSeriesOfDoses
            )
            certificateModel.recoveryStatements?.isNotEmpty() == true -> context.getString(R.string.type_recovered)
            certificateModel.tests?.isNotEmpty() == true -> context.getString(R.string.type_test)
            else -> context.getString(R.string.type_test)
        }
    }

    private fun setUpPossibleLimitation(verificationError: VerificationError?) {
        if (verificationError == null) {
            View.GONE
        } else {
            binding.possibleLimitationsName.text = context.getString(
                when (verificationError) {
                    VerificationError.GREEN_CERTIFICATE_EXPIRED -> R.string.certificate_is_expired
                    VerificationError.CERTIFICATE_REVOKED -> R.string.certificate_was_revoked
                    VerificationError.VERIFICATION_FAILED -> R.string.verification_failed
                    VerificationError.CERTIFICATE_EXPIRED -> R.string.signing_certificate_is_expired
                    VerificationError.TEST_DATE_IS_IN_THE_FUTURE -> R.string.the_test_date_is_in_the_future
                    VerificationError.TEST_RESULT_POSITIVE -> R.string.test_result_positive
                    VerificationError.RECOVERY_NOT_VALID_SO_FAR -> R.string.recovery_not_valid_yet
                    VerificationError.RECOVERY_NOT_VALID_ANYMORE -> R.string.recover_not_valid_anymore
                    VerificationError.RULES_VALIDATION_FAILED -> R.string.rules_validation_failed
                    VerificationError.CRYPTOGRAPHIC_SIGNATURE_INVALID -> R.string.cryptographic_signature_invalid
                }
            )
            View.VISIBLE
        }.apply {
            binding.possibleLimitationsTitle.visibility = this
            binding.possibleLimitationsName.visibility = this
        }
    }

    private fun setUpDateOfBirth(certificateModel: CertificateModel) {
        val dateOfBirth =
            certificateModel.dateOfBirth.parseFromTo(YEAR_MONTH_DAY, FORMATTED_YEAR_MONTH_DAY)
        if (dateOfBirth.isBlank()) {
            View.GONE
        } else {
            binding.dateOfBirthName.text = dateOfBirth
            View.VISIBLE
        }.apply {
            binding.dateOfBirthTitle.visibility = this
            binding.dateOfBirthName.visibility = this
        }
    }
}