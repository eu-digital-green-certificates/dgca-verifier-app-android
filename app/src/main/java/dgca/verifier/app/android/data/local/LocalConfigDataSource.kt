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
 *  Created by osarapulov on 5/17/21 8:21 AM
 */

package dgca.verifier.app.android.data.local

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dgca.verifier.app.android.data.Config
import timber.log.Timber
import java.io.*
import javax.inject.Inject


class LocalConfigDataSource @Inject constructor(@ApplicationContext private val context: Context) : MutableConfigDataSource {
    private lateinit var config: Config
    private val gson = Gson()

    companion object {
        const val DEFAULT_CONFIG_FILE = "verifier-context.jsonc"
        const val CONFIG_FILE = "config.json"
    }

    override fun setConfig(config: Config): Config {
        this.config = config
        return saveConfig(this.config)
    }

    override fun getConfig(): Config {
        if (!this::config.isInitialized) {
            try {
                config = loadConfig()
            } catch (error: Throwable) {
                Timber.v("Error loading config from local.")
            }
            if (!this::config.isInitialized) {
                config = defaultConfig()
            }
        }
        return config
    }

    private fun configFile(): File = File(context.filesDir, CONFIG_FILE)

    private fun loadConfig(): Config = BufferedReader(InputStreamReader(FileInputStream(configFile()))).use {
        gson.fromJson(it.readText(), Config::class.java)
    }

    private fun saveConfig(config: Config): Config {
        FileWriter(configFile()).use {
            gson.toJson(config, it)
        }
        return config
    }

    private fun defaultConfig(): Config {
        return context.assets.open(DEFAULT_CONFIG_FILE).bufferedReader().use { gson.fromJson(it.readText(), Config::class.java) }
    }
}