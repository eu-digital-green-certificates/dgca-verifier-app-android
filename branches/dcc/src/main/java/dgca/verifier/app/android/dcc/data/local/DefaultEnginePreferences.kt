package dgca.verifier.app.android.dcc.data.local

import dgca.verifier.app.engine.data.source.local.EnginePreferences

class DefaultEnginePreferences(private val preferences: Preferences): EnginePreferences {
    override fun setLastCountriesSync(millis: Long) {
        preferences.lastCountriesSyncTimeMillis = millis
    }

    override fun getLastCountriesSync(): Long = preferences.lastCountriesSyncTimeMillis

    override fun getSelectedCountryIsoCode(): String? = preferences.selectedCountryIsoCode
}
