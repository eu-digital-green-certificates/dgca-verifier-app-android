package dgca.verifier.app.android.diia.settings.debug

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccSelectCountryData(
    val availableCountriesIsoCodes: Set<String> = emptySet(),
    val selectedCountryIsoCode: String? = null
) : Parcelable