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
 *  Created by mykhailo.nester on 31/03/2022, 19:44
 */

package dgca.verifier.app.android.vc

import java.util.zip.DataFormatException
import java.util.zip.Inflater

@Throws(DataFormatException::class)
fun inflate(inputBytes: ByteArray): ByteArray {
    val inflatedBytes = ByteArray(inputBytes.size * 10)

    /*
     * As we are using the 'nowrap' option,  it is necessary to provide an extra "dummy" byte as input.
     * Seems to be working even without this extra dummy byte. But javadoc recommends to add a dummy byte.
     * Adding 0 add the dummy byte at the end of the input byte array.
     */
    val inputWithDummyByteAdded: ByteArray = inputBytes.copyOf(inputBytes.size + 1)

    // Set nowrap to true, to ignore the ZLIB headers
    val inflater = Inflater(true)
    inflater.setInput(inputWithDummyByteAdded)
    val numberInflatedBytes = inflater.inflate(inflatedBytes)
    inflater.end()

    return inflatedBytes.copyOf(numberInflatedBytes)
}