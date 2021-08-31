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
 *  Created by osarapulov on 8/31/21 10:58 AM
 */

package dgca.verifier.app.android.verification.detailed

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.ViewDetailedVerificationResultBinding
import dgca.verifier.app.android.verification.VerificationComponent
import dgca.verifier.app.android.verification.VerificationComponentState


class DetailedVerificationResultView(context: Context, attrs: AttributeSet?) :
    CardView(context, attrs) {
    private val binding: ViewDetailedVerificationResultBinding =
        ViewDetailedVerificationResultBinding.inflate(LayoutInflater.from(context), this)

    init {
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            context.resources.getDimension(R.dimen.detailed_verification_result_banner_radius),
            context.resources.displayMetrics
        )
    }

    fun setUp(verificationComponentStates: Map<VerificationComponent, VerificationComponentState>) {
        binding.techVerificationImage.setImageResource(verificationComponentStates[VerificationComponent.TECHNICAL_VERIFICATION]!!.getAsset())
        binding.issuerInvalidationImage.setImageResource(verificationComponentStates[VerificationComponent.ISSUER_INVALIDATION]!!.getAsset())
        binding.destinationAcceptanceImage.setImageResource(verificationComponentStates[VerificationComponent.DESTINATION_INVALIDATION]!!.getAsset())
        binding.travellerAcceptanceImage.setImageResource(verificationComponentStates[VerificationComponent.TRAVELLER_ACCEPTANCE]!!.getAsset())
    }

    @DrawableRes
    fun VerificationComponentState.getAsset(): Int = when (this) {
        VerificationComponentState.FAILED -> R.drawable.ic_traffic_fail
        VerificationComponentState.OPEN -> R.drawable.ic_traffic_uncertain
        VerificationComponentState.PASSED -> R.drawable.ic_traffic_success
    }
}