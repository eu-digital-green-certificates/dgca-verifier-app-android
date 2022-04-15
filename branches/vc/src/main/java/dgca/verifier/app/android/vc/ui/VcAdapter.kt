package dgca.verifier.app.android.vc.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.app.vc.databinding.ItemMainDataBinding
import dgca.verifier.app.android.vc.ui.model.DataItem

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
            binding.value.text = data.value
        }
    }
}
