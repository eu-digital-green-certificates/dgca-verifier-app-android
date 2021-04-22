package dgca.verifier.app.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.decoder.chain.model.Vaccination

enum class DataType {
    TEST, VACCINATION, RECOVERED
}

class MediaListAdapter(
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
            return oldList[oldItemPosition].date == newList[newItemPosition].date
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