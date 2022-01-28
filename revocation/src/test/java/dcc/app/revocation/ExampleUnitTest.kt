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
 *  Created by mykhailo.nester on 23/12/2021, 13:49
 */

package dcc.app.revocation

import dcc.app.revocation.validation.BloomFilterImpl
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suppress("UnstableApiUsage")
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun bloomFilterTest() {
        val hash1 = "1b4f0e9851971998e732078544c96b36c3d01cedf7caa332359d6f1d83567014"
        val hash2 = "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752"
        val hash3 = "fd61a03af4f77d870fc21e05e7e80678095c92d808cfb3b5c279ee04c74aca13"

        val dccHash = "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752"
        val randomHash = "a441b15fe9a3cf56661190a0b93b9dec7d04127288cc87250967cf3b52894d11"

        val impl = BloomFilterImpl(3, 0.1f)
        impl.add(hash1.toByteArray())
        impl.add(hash2.toByteArray())
        impl.add(hash3.toByteArray())

        assertTrue(impl.mightContain(dccHash.toByteArray()))        // True
        assertFalse(impl.mightContain(randomHash.toByteArray()))    // False

        val stream = ByteArrayOutputStream()
        impl.writeTo(stream)
        val filterByteArray = stream.toByteArray()
        val inputStream: InputStream = ByteArrayInputStream(filterByteArray)
        val bloomFilterNew = BloomFilterImpl(inputStream)

        assertTrue(bloomFilterNew.mightContain(dccHash.toByteArray()))      // True
        assertFalse(bloomFilterNew.mightContain(randomHash.toByteArray()))  // False
    }
}