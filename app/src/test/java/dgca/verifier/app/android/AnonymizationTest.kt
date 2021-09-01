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
 *  Created by mykhailo.nester on 30/08/2021, 23:04
 */

package dgca.verifier.app.android

import dgca.verifier.app.android.anonymization.AnonymizationManager
import dgca.verifier.app.android.anonymization.anonymize
import org.junit.Test

class AnonymizationTest {

    @Test
    fun anonymizeTextTest() {
        val anonymizationManager = AnonymizationManager()
        val text = "Title lowercase UPPERCASE " +
                "Lm modifier: \u02B0 " +
                "Lo: \u0294 " +
                "Mc: \u0903 " +
                "Me Mn: \u0488 \u0300 " +
                "Nd: 0123456789 \u0663 " +
                "Nl: \u16EE " +
                "No: \u00B2 " +
                "\u002D: - " +
                "\u002E: . " +
                "\u002C: , " +
                "Pd: \u1400 " +
                "Pf Ps Pi Pe: \u00BB \u0028 \u00AB \u0029 " +
                "Po: : " +
                "Sc Sk Sm So: \u0024 \u005E \u002B \u00A6 " +
                "Space: \u0020 " +
                "Zs: \u2005 " +
                "Zp: \u2029 " +
                "Other: \u200E"

        val test = "2201-02-23"
        val dateOfBirth = test.substring(test.indexOf("-") + 1).anonymize()
        val newStr = test.replaceAfter("-", dateOfBirth)
        val test1 = "2201-02-23"

//        val result = anonymizationManager.anonymize()

//        assertThat(
//            result,
//            CoreMatchers.equalTo(
//                "Xxxxx xxxxxxxxx XXXXXXXXX " +
//                        "Xx xxxxxxxx! M " +
//                        "Xx! R " +
//                        "Xx! S " +
//                        "Xx Xx! s s " +
//                        "Xx! 9999999999 8 " +
//                        "Xx! 1 " +
//                        "Xx! 2 " +
//                        "-! - " +
//                        ".! . " +
//                        ",! , " +
//                        "Xx! = " +
//                        "Xx Xx Xx Xx! Q Q Q Q " +
//                        "Xx! ! " +
//                        "Xx Xx Xx Xx! @ @ @ @ " +
//                        "Xxxxx!   " +
//                        "Xx! _ " +
//                        "Xx! N " +
//                        "Xxxxx! Q"
//            )
//        )
    }
}