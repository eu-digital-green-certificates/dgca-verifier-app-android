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
 *  Created by climent on 6/14/21 3:19 PM
 */

package it.ministerodellasalute.verificaC19.di

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import it.ministerodellasalute.verificaC19.data.local.AppDatabase
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocalDataSourceModuleTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var context: Context

    private lateinit var module: LocalDataSourceModule

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        module = LocalDataSourceModule
    }

    @Test
    fun `test provideDb dependency`() {
        MatcherAssert.assertThat(
            "provideDb",
            module.provideDb(context),
            CoreMatchers.instanceOf(AppDatabase::class.java)
        )
    }

}