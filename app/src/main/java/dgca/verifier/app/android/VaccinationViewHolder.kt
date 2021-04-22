package dgca.verifier.app.android

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.verifier.app.android.databinding.ItemVaccinationBinding
import dgca.verifier.app.decoder.chain.model.Vaccination

class VaccinationViewHolder(private val binding: ItemVaccinationBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup) =
            VaccinationViewHolder(ItemVaccinationBinding.inflate(inflater, parent, false))
    }

    fun bind(data: Vaccination) {
        binding.disease.text = "Disease: ${data.disease}"
    }
}
