/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by mykhailo.nester on 21/01/2022, 14:45
 */

package dcc.app.revocation.data.local

import androidx.room.TypeConverter
import dcc.app.revocation.domain.model.DccRevocationHashType

class EnumConverter {

    @TypeConverter
    fun storedStringToEnum(value: String): Set<DccRevocationHashType> {
        val dbValues: List<String> = value.split("\\s*,\\s*".toRegex())
        val enums = mutableSetOf<DccRevocationHashType>()
        for (s in dbValues) {
            if (s.isNotEmpty()) {
                enums.add(DccRevocationHashType.valueOf(s))
            }
        }
        return enums
    }

    @TypeConverter
    fun languagesToStoredString(list: Set<DccRevocationHashType>): String {
        var value = ""
        list.iterator().forEach {
            value += it.name + ","
        }
        return value
    }
}