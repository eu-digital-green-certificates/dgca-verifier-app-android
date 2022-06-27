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
 *  Created by mykhailo.nester on 12/04/2022, 19:16
 */

package com.app.shc

import timber.log.Timber

fun String.convertNumericToJws(): String {
    return try {
        val numericBuilder = StringBuilder("")
        var index = 2
        while (index <= length) {
            numericBuilder.append(((substring(index - 2, index)).toInt() + 45).toChar())
            index += 2
        }
        numericBuilder.toString()
    } catch (ex: Exception) {
        Timber.d("Failed to parse")
        ""
    }
}
