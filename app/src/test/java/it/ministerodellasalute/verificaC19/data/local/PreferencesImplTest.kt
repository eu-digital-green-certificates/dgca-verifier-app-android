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
 *  Created by climent on 6/14/21 8:00 PM
 */

package it.ministerodellasalute.verificaC19.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PreferencesImplTest{

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var context: Context

    private lateinit var preferences: PreferencesImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        preferences = PreferencesImpl(context)
    }

    @Test
    fun `test clear all from preferances`() {
        preferences.resumeToken = 1L
        val expectedValue = 0L

        preferences.clear()

        Assert.assertEquals(expectedValue, preferences.resumeToken)
    }

    @Test
    fun `test clear all from preferances 2`() {
        preferences.validationRulesJson = "validationJson"
        val expectedValue = ""

        preferences.clear()

        Assert.assertEquals(expectedValue, preferences.validationRulesJson)
    }


}