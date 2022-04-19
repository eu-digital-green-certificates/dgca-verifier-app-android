/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 23/03/2022, 22:35
 */

package com.app.shc

import android.content.Intent
import android.net.Uri
import com.android.app.base.Processor
import com.android.app.base.RESULT_KEY
import timber.log.Timber
import javax.inject.Inject

class ShcProcessor @Inject constructor() : Processor {

    override fun prefetchData() {
        Timber.d("Prefetching data...")
    }

    override fun isApplicable(input: String): Intent? =
        if (input.startsWith(SMART_HEALTH_CARD_PREFIX)) {
            val shc = input.drop(SMART_HEALTH_CARD_PREFIX.length)
            val jws = decodeShc(shc)
            Intent(VC_VIEW_ACTION, Uri.parse(VC_VIEW_URI)).apply {
                putExtra(RESULT_KEY, jws)
            }
        } else {
            null
        }

    override fun getSettingsIntent(): Pair<String, Intent>? = null

    private fun decodeShc(shc: String) = shc.convertNumericToJws()

    companion object {
        private const val SMART_HEALTH_CARD_PREFIX = "shc:/"
        private const val VC_VIEW_ACTION = "com.android.app.vc.View"
        private const val VC_VIEW_URI = "verifier://vc"
    }
}
