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
 *  Created by mykhailo.nester on 18/04/2022, 12:38
 */

package dgca.verifier.app.android.vc

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import dgca.verifier.app.android.vc.model.DataItem
import timber.log.Timber

fun String.tryFetchObject(path: String, title: String, items: MutableList<DataItem>) {
    try {
        val jsonContext: DocumentContext = JsonPath.parse(this)
        val value = jsonContext.read(path, String::class.java)
        items.add(DataItem(title, listOf(value)))
    } catch (ex: Exception) {
        tryFetchObjects(path, title, items)
    }
}

private fun String.tryFetchObjects(path: String, title: String, items: MutableList<DataItem>) {
    try {
        val jsonContext: DocumentContext = JsonPath.parse(this)
        val typeRef: TypeRef<List<String>> = object : TypeRef<List<String>>() {}
        val value = jsonContext.read(path, typeRef)
        if (value.isNotEmpty()) {
            items.add(DataItem(title, value))
        }
    } catch (ex: Exception) {
        Timber.e(ex, "Cannot parse path")
    }
}


