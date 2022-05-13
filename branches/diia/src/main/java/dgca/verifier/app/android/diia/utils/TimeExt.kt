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
 *  Created by mykhailo.nester on 10/10/2021, 09:10
 */

package dgca.verifier.app.android.diia.utils

import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

const val YEAR_MONTH_DAY = "yyyy-MM-dd"
const val FORMATTED_YEAR_MONTH_DAY = "MMM d, yyyy"
private const val FORMATTED_DATE_TIME = "MMM d, yyyy, HH:mm"
private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(FORMATTED_DATE_TIME)

private fun String.toZonedDateTime(): ZonedDateTime? =
    try {
        ZonedDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

private fun String.toLocalDateTime(): LocalDateTime? =
    try {
        LocalDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

fun String.toFormattedDateTime(): String {
    if (isEmpty()) {
        return ""
    }

    return "${parseDateOfCollectionToUtcTimestamp()} (UTC)"
}

fun String.parseFromTo(from: String, to: String): String =
    try {
        val parser = SimpleDateFormat(from, Locale.US)
        val formatter = SimpleDateFormat(to, Locale.US)

        formatter.format(parser.parse(this)!!)
    } catch (ex: Exception) {
        ""
    }

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.formatWith(pattern: String): String =
    DateTimeFormatter.ofPattern(pattern).format(this)

fun String?.parseDateOfCollectionToUtcTimestamp(): String {
    if (isNullOrEmpty()) {
        return ""
    }

    return try {
        val dateTime =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this, OffsetDateTime::from)
                .withOffsetSameInstant(ZoneOffset.UTC)
        DATE_TIME_FORMATTER.format(dateTime)
    } catch (ex: Exception) {
        try {
            val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssSX")
                .parse(this, OffsetDateTime::from)
                .withOffsetSameInstant(ZoneOffset.UTC)
            DATE_TIME_FORMATTER.format(dateTime)
        } catch (ex: Exception) {
            ""
        }
    }
}
