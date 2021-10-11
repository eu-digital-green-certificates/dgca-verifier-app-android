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
 *  Created by osarapulov on 5/28/21 12:29 AM
 */

package dgca.verifier.app.android

import dgca.verifier.app.android.utils.toFormattedDateTime
import org.junit.Assert
import org.junit.Test

class TimeExtTest {
    @Test
    fun testLocalToFormattedDateTime() {
        val localDateTimeString = "2021-05-25T09:02:07"
        val expectedFormattedLocalDateTime = "May 25, 2021, 09:02 (UTC)"

        val actualFormattedLocalDateTime = localDateTimeString.toFormattedDateTime()

        Assert.assertEquals(expectedFormattedLocalDateTime, actualFormattedLocalDateTime)
    }

    @Test
    fun testZonedToFormattedDateTime() {
        val zonedDateTimeString = "2021-05-19T08:20:00Z"
        val expectedFormattedLocalDateTime = "May 19, 2021, 08:20 (UTC)"

        val actualFormattedLocalDateTime = zonedDateTimeString.toFormattedDateTime()

        Assert.assertEquals(expectedFormattedLocalDateTime, actualFormattedLocalDateTime)
    }

    @Test
    fun testCustomDateTime() {
        val zonedDateTimeString = "1 1 2021"

        val actualFormattedLocalDateTime = zonedDateTimeString.toFormattedDateTime()

        Assert.assertNull(actualFormattedLocalDateTime)
    }
}