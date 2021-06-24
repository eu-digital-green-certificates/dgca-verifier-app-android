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
 *  Created by osarapulov on 6/18/21 8:57 AM
 */

package dgca.verifier.app.android.verification.rules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemRuleValidationResultBinding
import dgca.verifier.app.android.databinding.ItemRuleValidationResultHeaderBinding

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 18.06.21 8:57
 */
class RuleValidationResultsAdapter(
    private val inflater: LayoutInflater,
    ruleValidationResultCards: Collection<RuleValidationResultCard>
) :
    RecyclerView.Adapter<RuleValidationResultsAdapter.ViewHolder>() {

    private val ruleValidationResultCards: MutableList<RuleValidationResultCard> =
        ruleValidationResultCards.toMutableList()

    sealed class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {


        class HeaderViewHolder(binding: ItemRuleValidationResultHeaderBinding) :
            ViewHolder(binding.root) {
            companion object {
                fun create(inflater: LayoutInflater, parent: ViewGroup) =
                    HeaderViewHolder(
                        ItemRuleValidationResultHeaderBinding.inflate(
                            inflater,
                            parent,
                            false
                        )
                    )
            }
        }

        class CardViewHolder(private val binding: ItemRuleValidationResultBinding) :
            ViewHolder(binding.root) {
            fun bind(ruleValidationResultCard: RuleValidationResultCard) {
                binding.identifier.text = ruleValidationResultCard.identifier
                binding.description.text = ruleValidationResultCard.description
                binding.result.text = ruleValidationResultCard.result
                binding.current.text = ruleValidationResultCard.current
            }

            companion object {
                fun create(inflater: LayoutInflater, parent: ViewGroup) =
                    CardViewHolder(ItemRuleValidationResultBinding.inflate(inflater, parent, false))
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0) CARD_TYPE else HEADER_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            HEADER_TYPE -> ViewHolder.HeaderViewHolder.create(inflater, parent)
            CARD_TYPE -> ViewHolder.CardViewHolder.create(inflater, parent)
            else -> ViewHolder.CardViewHolder.create(inflater, parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ViewHolder.CardViewHolder) {
            holder.bind(ruleValidationResultCards[position - 1])
        }
    }

    override fun getItemCount(): Int =
        if (ruleValidationResultCards.isEmpty()) 0 else ruleValidationResultCards.size + 1

    companion object {
        const val HEADER_TYPE = 0
        const val CARD_TYPE = 1
    }
}
