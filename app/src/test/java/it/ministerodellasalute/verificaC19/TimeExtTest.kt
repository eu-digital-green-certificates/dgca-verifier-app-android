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
 *  Created by climent on 6/14/21 1:49 PM
 */

package it.ministerodellasalute.verificaC19

import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class TimeExtTest {

    @Test
    fun `test format person birthday`() {
        val localDateTimeString = "2021-05-25"
        val expectedFormattedLocalDateTime = "25/05/2021"

        val actualFormattedLocalDateTime = localDateTimeString.parseFromTo(YEAR_MONTH_DAY, FORMATTED_BIRTHDAY_DATE)

        Assert.assertEquals(expectedFormattedLocalDateTime, actualFormattedLocalDateTime)
    }


    @Test
    fun `test format person birthday when date have a wrong format`() {
        val localDateTimeString = "2021-05+25"
        val expectedFormattedLocalDateTime = ""

        val actualFormattedLocalDateTime = localDateTimeString.parseFromTo(YEAR_MONTH_DAY, FORMATTED_BIRTHDAY_DATE)

        Assert.assertEquals(expectedFormattedLocalDateTime, actualFormattedLocalDateTime)
    }

    @Test
    fun `test timestamp to last update format`() {
        val localDateTime = 1623668633998
        val expectedFormattedLocalDateTime = "14/06/2021, 13:03"

        val actualFormattedLocalDateTime = localDateTime.parseTo(FORMATTED_DATE_LAST_SYNC)

        val sdf = SimpleDateFormat("dd/MM/yyyy', 'HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj: Date = sdf.parse(actualFormattedLocalDateTime)
        val expectedFormattedLocalDateTimeToUTC: Date = sdf.parse(expectedFormattedLocalDateTime)
        val formattedDateObj = SimpleDateFormat("dd/MM/yyyy', 'HH:mm").format(dateObj)
        val formattedExpectedFormattedLocalDateTimeToUTC = SimpleDateFormat("dd/MM/yyyy', 'HH:mm").format(expectedFormattedLocalDateTimeToUTC);

        Assert.assertEquals(formattedExpectedFormattedLocalDateTimeToUTC, formattedDateObj)
    }

}