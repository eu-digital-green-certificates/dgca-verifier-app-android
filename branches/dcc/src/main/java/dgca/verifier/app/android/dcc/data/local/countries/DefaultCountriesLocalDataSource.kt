/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 2:23 PM
 */

package dgca.verifier.app.android.dcc.data.local.countries

import dgca.verifier.app.android.dcc.data.local.model.CountryLocal
import dgca.verifier.app.engine.data.source.local.EnginePreferences
import dgca.verifier.app.engine.data.source.local.countries.CountriesLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class DefaultCountriesLocalDataSource(
    private val countriesDao: CountriesDao, private val enginePreferences: EnginePreferences
) : CountriesLocalDataSource {

    override suspend fun updateCountries(countriesIsoCodes: List<String>) {
        countriesDao.apply {
            deleteAll()
            insertAll(*countriesIsoCodes.map { it.toCountryLocal() }.toTypedArray())
        }
    }

    override fun getCountries(): Flow<List<String>> =
        countriesDao.getAll().map { it.map { countryLocal -> countryLocal.toCountry() } }

    override fun getLastCountriesSync(): Long = enginePreferences.getLastCountriesSync()

    override fun getSelectedCountryIsoCode(): String? =
        enginePreferences.getSelectedCountryIsoCode()
}

fun String.toCountryLocal(): CountryLocal = CountryLocal(isoCode = lowercase(Locale.ROOT))

fun CountryLocal.toCountry(): String = isoCode
