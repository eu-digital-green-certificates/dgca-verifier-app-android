/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
 *  ---
 *  Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
 *  Created by mykhailo.nester on 18/04/2022, 13:21
 */

package dgca.verifier.app.android.vc.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.app.vc.databinding.ItemMainDataBinding
import dgca.verifier.app.android.vc.model.DataItem

class VcAdapter(
    private val inflater: LayoutInflater
) : ListAdapter<DataItem, VcAdapter.VcViewHolder>(AsyncDifferConfig.Builder(BaseDiffCallback()).build()) {

    private var items = emptyList<DataItem>()

    class BaseDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VcViewHolder =
        VcViewHolder(ItemMainDataBinding.inflate(inflater, parent, false))

    override fun onBindViewHolder(holder: VcViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun update(list: List<DataItem>) {
        items = list
        submitList(list)
    }

    inner class VcViewHolder(private val binding: ItemMainDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: DataItem) {
            binding.title.text = data.title
            var result = ""
            data.value.forEachIndexed { index, s -> result += "${index + 1}: $s \n" }
            binding.value.text = result
        }
    }
}