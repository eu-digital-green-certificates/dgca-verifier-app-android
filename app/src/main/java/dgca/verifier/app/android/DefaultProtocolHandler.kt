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
 *  Created by osarapulov on 3/17/22, 7:57 AM
 */

package dgca.verifier.app.android

import android.content.Intent
import com.android.app.base.Processor

class DefaultProtocolHandler(private val processors: Set<Processor>) : ProtocolHandler {

    override fun prefetchData() {
        processors.forEach { it.prefetchData() }
    }

    override fun handle(input: String): Intent? {
        val result: Intent? = null
        processors.forEach { processor ->
            processor.isApplicable(input)?.let { return it }
        }
        return result
    }

    override fun getSettingsIntents(): List<Pair<String, Intent>> =
        processors.mapNotNull { processor -> processor.getSettingsIntent() }
}
