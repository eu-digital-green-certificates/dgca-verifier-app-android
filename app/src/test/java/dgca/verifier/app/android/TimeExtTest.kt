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
    fun testTimeToUTC_ISO8601_format_SuccessTest() {
        val format1 = "2021-08-20T05:03:12Z"      // (UTC time)
        val format2 = "2021-08-20T07:03:12+02"    // (CEST time)
        val format3 = "2021-08-20T07:03:12+0200"  // (CEST time)
        val format4 = "2021-08-20T07:03:12+02:00" // (CEST time)

        val result1 = format1.toFormattedDateTime()
        val result2 = format2.toFormattedDateTime()
        val result3 = format3.toFormattedDateTime()
        val result4 = format4.toFormattedDateTime()

        val expectedResult = "Aug 20, 2021, 05:03 (UTC)"

        Assert.assertEquals(expectedResult, result1)
        Assert.assertEquals(expectedResult, result2)
        Assert.assertEquals(expectedResult, result3)
        Assert.assertEquals(expectedResult, result4)
    }
}