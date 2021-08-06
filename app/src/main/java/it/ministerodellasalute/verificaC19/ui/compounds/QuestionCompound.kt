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

package it.ministerodellasalute.verificaC19.ui.compounds

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import it.ministerodellasalute.verificaC19.databinding.CompoundQuestionBinding

class QuestionCompound(context: Context?) : CardView(context!!) {
    private var binding: CompoundQuestionBinding =
        CompoundQuestionBinding.inflate(LayoutInflater.from(context), this, true)

    fun setupWithLabels(text: String, relatedLink: String) {
        clipToPadding = false;
        binding.questionCard.clipToPadding = false
        binding.questionText.text = SpannableString(text).also {
            it.setSpan(UnderlineSpan(), 0, it.length, 0)
            it.setSpan(StyleSpan(Typeface.BOLD), 0, it.length, 0)
        }
        binding.questionCard.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(relatedLink))
            startActivity(context, browserIntent, null)
        }
    }
}