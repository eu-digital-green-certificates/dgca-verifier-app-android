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
 *  Created by mykhailo.nester on 5/5/21 11:57 PM
 */

package dgca.verifier.app.android

import java.text.SimpleDateFormat
import java.util.Locale

const val YEAR_MONTH_DAY = "yyyy-MM-dd"
const val DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val FORMATTED_YEAR_MONTH_DAY = "MMM d, yyyy"
const val FORMATTED_DATE_TIME = "MMM d, yyyy, HH:mm"

fun String.parseFromTo(from: String, to: String): String {
    return try {
        val parser = SimpleDateFormat(from, Locale.US)
        val formatter = SimpleDateFormat(to, Locale.US)
        return formatter.format(parser.parse(this)!!)
    } catch (ex: Exception) {
        ""
    }
}