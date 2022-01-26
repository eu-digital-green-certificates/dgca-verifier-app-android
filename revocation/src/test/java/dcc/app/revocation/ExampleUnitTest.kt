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

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import dcc.app.revocation.validation.BloomFilterImpl
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

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

    //    TODO: check implementation
    @Test
    fun bloomFilterTest() {
        val hash1 = "1b4f0e9851971998e732078544c96b36c3d01cedf7caa332359d6f1d83567014"
        val hash2 = "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752"
        val hash3 = "fd61a03af4f77d870fc21e05e7e80678095c92d808cfb3b5c279ee04c74aca13"

        val dccHash = "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752"
        val randomHash = "a441b15fe9a3cf56661190a0b93b9dec7d04127288cc87250967cf3b52894d11"

//        val filter = BloomFilter.create(
//            Funnels.stringFunnel(Charset.defaultCharset()),
//            3,
//            0.01
//        )
//
//        filter.put(hash1)
//        filter.put(hash2)
//        filter.put(hash3)
//
//        assertThat(filter.mightContain(dccHash), `is`(true))
//        assertThat(filter.mightContain(randomHash), `is`(false))

        val impl = BloomFilterImpl(3, 3.toByte(), 3)
        impl.add(hash1.toByteArray())
        impl.add(hash2.toByteArray())
        impl.add(hash3.toByteArray())

        val stream: ByteArrayOutputStream = ByteArrayOutputStream()
        impl.writeTo(stream)

        assert(impl.mightContain(dccHash.toByteArray()))

        val test1 = stream.toByteArray().toHexString()
        val array = test1.hexToByteArray()

        val inputStream: InputStream = ByteArrayInputStream(array)
        val test = BloomFilterImpl(inputStream)

        assert(impl.mightContain(dccHash.toByteArray()))

        assert(impl.mightContain(dccHash.toByteArray()))
        assert(impl.data.length() == 3)
    }

    fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    fun String.hexToByteArray(): ByteArray = chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}