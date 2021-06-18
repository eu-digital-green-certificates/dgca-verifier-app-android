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
 *  Created by climent on 6/14/21 2:47 PM
 */

package it.ministerodellasalute.verificaC19.di

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import kotlinx.coroutines.Dispatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DispatcherProviderTest{
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var module: DispatcherProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        module = DispatcherProvider()
    }

    @Test
    fun `test getIO dependency`() {
        MatcherAssert.assertThat(
            "getIO",
            module.getIO(),
            CoreMatchers.instanceOf(Dispatchers.IO::class.java)
        )
    }

    @Test
    fun `test getDefault dependency`() {
        MatcherAssert.assertThat(
            "getDefault",
            module.getDefault(),
            CoreMatchers.instanceOf(Dispatchers.Default::class.java)
        )
    }
}