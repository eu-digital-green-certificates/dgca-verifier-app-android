/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 2:43 PM
 */

package dgca.verifier.app.android.diia.data

import com.android.app.diia.BuildConfig
import dgca.verifier.app.android.diia.data.local.MutableConfigDataSource
import dgca.verifier.app.android.diia.data.remote.RemoteConfigDataSource
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val localConfigDataSource: MutableConfigDataSource,
    private val remoteConfigDataSource: RemoteConfigDataSource
) : ConfigRepository {

    override fun local(): ConfigDataSource = localConfigDataSource

    override fun getConfig(): Config =
        remoteConfigDataSource.getConfig(
            localConfigDataSource.getConfig().getContextUrl(BuildConfig.VERSION_NAME)
        ).apply {
            localConfigDataSource.setConfig(this)
        }
}
