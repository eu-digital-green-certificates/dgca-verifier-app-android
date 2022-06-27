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
 *  Created by osarapulov on 9/4/21 8:26 PM
 */

package dgca.verifier.app.android.dcc.settings.debug.mode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountriesData(
    val availableCountriesCodes: Set<String>,
    val selectedCountriesCodes: Set<String>
) : Parcelable
