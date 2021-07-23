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
 *  Created by osarapulov on 7/22/21 8:42 PM
 */

package dgca.verifier.app.android

import android.view.View
import android.widget.TextView
import java.util.*


fun String.bindCountryWith(countryTitleView: View, countryValueView: TextView) {
    val issuerCountry =
        if (this.isNotBlank()) Locale("", this).displayCountry else ""
    issuerCountry.apply {
        if (this.isNotBlank()) {
            countryValueView.text = this
            View.VISIBLE
        } else {
            View.GONE
        }.apply {
            countryTitleView.visibility = this
            countryValueView.visibility = this
        }
    }
}

fun String.bindText(titleView: View, valueView: TextView) = apply {
    if (this.isNotBlank()) {
        valueView.text = this
        View.VISIBLE
    } else {
        View.GONE
    }.apply {
        titleView.visibility = this
        valueView.visibility = this
    }
}