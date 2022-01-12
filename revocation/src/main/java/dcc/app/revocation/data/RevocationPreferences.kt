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

package dcc.app.revocation.data

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface RevocationPreferences {

    var eTag: String?

    fun clear()
    fun putLastModifiedForKid(kid: String, lastModified: String)
    fun getLastModifiedForKid(kid: String): String
}

/**
 * [RevocationPreferences] impl backed by [android.content.SharedPreferences].
 */
class RevocationPreferencesImpl(context: Context) : RevocationPreferences {

    private var preferences: Lazy<SharedPreferences> = lazy {
        context.applicationContext.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE)
    }

    override var eTag: String? by StringPreference(
        preferences,
        KEY_ETAG
    )

    override fun clear() {
        preferences.value.edit().clear().apply()
    }

    override fun putLastModifiedForKid(kid: String, lastModified: String) {
        preferences.value.edit { putString(kid, lastModified) }
    }

    override fun getLastModifiedForKid(kid: String): String = preferences.value.getString(kid, "") ?: ""

    companion object {
        private const val USER_PREF = "dcc.revocation.app.pref"
        private const val KEY_ETAG = "dcc.revocation.app.pref.eTag"
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