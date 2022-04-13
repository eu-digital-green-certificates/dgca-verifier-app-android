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
 *  Created by mykhailo.nester on 4/27/21 10:41 PM
 */

package dgca.verifier.app.android.dcc.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Preferences {

    var resumeToken: Long

    var lastKeysSyncTimeMillis: Long

    var lastCountriesSyncTimeMillis: Long

    var selectedCountryIsoCode: String?

    var debugModeState: String?

    var debugModeSelectedCountriesCodes: Set<String>?

    fun clear()
}

/**
 * [Preferences] impl backed by [android.content.SharedPreferences].
 */
class PreferencesImpl(context: Context) : Preferences {

    private var preferences: Lazy<SharedPreferences> = lazy {
        context.applicationContext.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
    }

    override var resumeToken by LongPreference(preferences, KEY_RESUME_TOKEN, -1)
    override var lastKeysSyncTimeMillis by LongPreference(
        preferences,
        KEY_LAST_KEYS_SYNC_TIME_MILLIS,
        -1
    )

    override var lastCountriesSyncTimeMillis by LongPreference(
        preferences,
        KEY_COUNTRIES_KEYS_SYNC_TIME_MILLIS,
        -1
    )

    override var selectedCountryIsoCode: String? by StringPreference(
        preferences,
        KEY_SELECTED_COUNTRY_ISO_CODE
    )
    override var debugModeState: String? by StringPreference(
        preferences,
        KEY_DEBUG_MODE_STATE
    )
    override var debugModeSelectedCountriesCodes: Set<String>? by StringSetPreference(
        preferences,
        KEY_DEBUG_MODE_SELECTED_COUNTRIES_CODES
    )

    override fun clear() {
        preferences.value.edit().clear().apply()
    }

    companion object {
        private const val USER_PREF = "dgca.verifier.app.pref"
        private const val KEY_RESUME_TOKEN = "resume_token"
        private const val KEY_LAST_KEYS_SYNC_TIME_MILLIS = "last_keys_sync_time_millis"
        private const val KEY_COUNTRIES_KEYS_SYNC_TIME_MILLIS = "last_countries_sync_time_millis"
        private const val KEY_SELECTED_COUNTRY_ISO_CODE = "selected_country_iso_code"
        private const val KEY_DEBUG_MODE_STATE = "debug_mode_state"
        private const val KEY_DEBUG_MODE_SELECTED_COUNTRIES_CODES =
            "debug_mode_selected_countries_codes"
    }
}

class LongPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Long
) : ReadWriteProperty<Any, Long> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.value.getLong(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.value.edit { putLong(name, value) }
    }
}

class StringPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: String? = null
) : ReadWriteProperty<Any, String?> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.value.getString(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.value.edit { putString(name, value) }
    }
}

class StringSetPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Set<String> = emptySet()
) : ReadWriteProperty<Any, Set<String>?> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Set<String>? {
        return if (preferences.value.contains(name)) preferences.value.getStringSet(
            name,
            defaultValue
        ) else null
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Set<String>?) {
        preferences.value.edit {
            value?.let {
                putStringSet(name, value)
            } ?: remove(name)
        }
    }
}
