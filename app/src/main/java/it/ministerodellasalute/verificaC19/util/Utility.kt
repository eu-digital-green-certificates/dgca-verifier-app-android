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
 *  Created by danieliulianrotaru on 5/25/21 3:44 PM
 */

package it.ministerodellasalute.verificaC19.util

class Utility {
    companion object {
        fun versionCompare(v1: String, v2: String): Int {
            // vnum stores each numeric part of version
            var vnum1 = 0
            var vnum2 = 0

            // loop until both String are processed
            var i = 0
            var j = 0
            while (i < v1.length || j < v2.length) {

                // Store numeric part of version 1 in vnum1
                while (i < v1.length && v1[i] != '.') {
                    vnum1 = (vnum1 * 10 + (v1[i] - '0'))
                    i++
                }

                // store numeric part of version 2 in vnum2
                while (j < v2.length && v2[j] != '.') {
                    vnum2 = (vnum2 * 10 + (v2[j] - '0'))
                    j++
                }
                if (vnum1 > vnum2) return 1
                if (vnum2 > vnum1) return -1

                // if equal, reset variables and go for next numeric part
                vnum2 = 0
                vnum1 = vnum2
                i++
                j++
            }
            return 0
        }
    }
}