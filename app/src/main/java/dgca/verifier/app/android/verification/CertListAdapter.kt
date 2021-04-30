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
 *  Created by mykhailo.nester on 4/24/21 5:18 PM
 */

package dgca.verifier.app.android.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.decoder.model.Vaccination

enum class DataType {
    TEST, VACCINATION, RECOVERED
}

class CertListAdapter(
    private val inflater: LayoutInflater
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = emptyList<Vaccination>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DataType.VACCINATION.ordinal -> VaccinationViewHolder.create(inflater, parent)
            else -> throw IllegalArgumentException("View type not defined")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = items[position]
        when (holder) {
            is VaccinationViewHolder -> holder.bind(data)
        }
    }

    override fun getItemViewType(position: Int): Int = DataType.VACCINATION.ordinal

    fun update(list: List<Vaccination>) {
        notifyChanges(items, list)
        items = list
    }
}

fun RecyclerView.Adapter<out RecyclerView.ViewHolder>.notifyChanges(
    oldList: List<Vaccination>,
    newList: List<Vaccination>
) {
    val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].dateOfVaccination == newList[newItemPosition].dateOfVaccination
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
            return Bundle()
        }
    })

    diff.dispatchUpdatesTo(this)
}