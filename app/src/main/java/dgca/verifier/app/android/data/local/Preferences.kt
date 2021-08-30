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

package dgca.verifier.app.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Preferences {

    var resumeToken: Long

    var lastKeysSyncTimeMillis: Long

    var selectedCountryIsoCode: String?

    var isDebugModeEnabled: Boolean?

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
    override var selectedCountryIsoCode: String? by StringPreference(
        preferences,
        KEY_SELECTED_COUNTRY_ISO_CODE
    )
    override var isDebugModeEnabled: Boolean? by BooleanPreference(
        preferences,
        KEY_IS_DEBUG_MODE_ENABLED
    )

    override fun clear() {
        preferences.value.edit().clear().apply()
    }

    companion object {
        private const val USER_PREF = "dgca.verifier.app.pref"
        private const val KEY_RESUME_TOKEN = "resume_token"
        private const val KEY_LAST_KEYS_SYNC_TIME_MILLIS = "last_keys_sync_time_millis"
        private const val KEY_SELECTED_COUNTRY_ISO_CODE = "selected_country_iso_code"
        private const val KEY_IS_DEBUG_MODE_ENABLED = "is_debug_mode_enabled"
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

class BooleanPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: Boolean = false
) : ReadWriteProperty<Any, Boolean?> {

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean? {
        return if (preferences.value.contains(name)) preferences.value.getBoolean(
            name,
            defaultValue
        ) else null
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean?) {
        preferences.value.edit {
            value?.let {
                putBoolean(name, value)
            } ?: remove(name)
        }
    }
}