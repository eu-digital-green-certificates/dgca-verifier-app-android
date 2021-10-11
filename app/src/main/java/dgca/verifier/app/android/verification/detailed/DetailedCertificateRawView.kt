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
 *  Created by osarapulov on 9/3/21 6:53 AM
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
import dgca.verifier.app.android.R
import dgca.verifier.app.android.databinding.ViewDetailedCertificateRawViewBinding

class DetailedCertificateRawView(context: Context, attrs: AttributeSet?) : MaterialCardView(context, attrs) {

    private val binding: ViewDetailedCertificateRawViewBinding =
        ViewDetailedCertificateRawViewBinding.inflate(LayoutInflater.from(context), this)

    private var isExpanded = false
    private lateinit var hcert: String

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
        }
    }

    private fun setExpanded(expanded: Boolean) {
        binding.expandButton.setImageResource(if (expanded) R.drawable.ic_icon_minus else R.drawable.ic_icon_plus)
        binding.hcert.visibility = if (expanded) View.VISIBLE else View.GONE
    }

    fun setHcert(hcert: String) {
        this.hcert = hcert
        binding.hcert.text = hcert
    }
}