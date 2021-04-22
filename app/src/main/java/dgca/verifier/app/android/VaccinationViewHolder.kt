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
        binding.disease.text = "disease: ${data.disease}"
        binding.vaccine.text = "vaccine: ${data.vaccine}"
        binding.medicinalProduct.text = "medicinalProduct: ${data.medicinalProduct}"
        binding.authorizationHolder.text = "authorizationHolder: ${data.authorizationHolder}"
        binding.doseSequence.text = "doseSequence: ${data.doseSequence}"
        binding.doseTotalNumber.text = "doseTotalNumber: ${data.doseTotalNumber}"
        binding.lotNumber.text = "lotNumber: ${data.lotNumber}"
        binding.date.text = "date: ${data.date}"
        binding.administeringCentre.text = "administeringCentre: ${data.administeringCentre}"
        binding.country.text = "country: ${data.country}"
    }
}
