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

import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor


const val YEAR_MONTH_DAY = "yyyy-MM-dd"
const val DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val FORMATTED_YEAR_MONTH_DAY = "MMM d, yyyy"
const val FORMATTED_DATE_TIME = "MMM d, yyyy, HH:mm"

fun String.parseFromTo(from: String, to: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern(to)
        val fmt = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd['T'[HH:mm:ss][.SSS][XXX]]"
        ) //all anticipated combinations
        val accessor: TemporalAccessor = fmt.parseBest(
            this,
            OffsetDateTime::from,  //most specific
            LocalDateTime::from,
            LocalDate::from //least specific
        )
        return formatter.format(accessor)
    } catch (ex: Exception) {
        ""
    }
}