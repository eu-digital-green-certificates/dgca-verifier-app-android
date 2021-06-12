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
 *  Created by climent on 6/7/21 3:04 PM
 */

package it.ministerodellasalute.verificaC19.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import it.ministerodellasalute.verificaC19.data.VerifierRepository
import it.ministerodellasalute.verificaC19.data.local.Preferences
import it.ministerodellasalute.verificaC19.utils.mock.ServiceMocks
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FirstViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var preferences: Preferences

    @RelaxedMockK
    private lateinit var verifierRepository: VerifierRepository

    private lateinit var viewModel: FirstViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = FirstViewModel(verifierRepository, preferences)
    }

    @Test
    fun `get AppMinVersion from server`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        assertEquals( viewModel.getAppMinVersion(), "4.1.1")
    }

    @Test
    fun `get Last Fetch Date when data not found`() {
        every { preferences.dateLastFetch}.returns(-1L)

        assertEquals( viewModel.getDateLastSync(), -1)
    }

    @Test
    fun `get Last Fetch Date when data is present`() {
        val currentDate = System.currentTimeMillis()

        every { preferences.dateLastFetch}.returns(currentDate)

        assertEquals( viewModel.getDateLastSync(), currentDate)
    }

    @Test
    fun `get Certificate Fetch Status when the request is not ready`() {
        val response = MutableLiveData(true)

        val mockObserver = mockk<Observer<Boolean>>()
        val slot = slot<Boolean>()
        val listOfResponse = arrayListOf<Boolean>()

        every { verifierRepository.getCertificateFetchStatus()}.returns(response)

        every { mockObserver.onChanged(capture(slot)) } answers {
            listOfResponse.add(slot.captured)
        }

        viewModel = FirstViewModel(verifierRepository, preferences)

        viewModel.fetchStatus.observeForever(mockObserver)

        assertEquals(true, listOfResponse[0])
    }

    @Test
    fun `get Certificate Fetch Status when the request is ready`() {
        val response = MutableLiveData(false)

        val mockObserver = mockk<Observer<Boolean>>()
        val slot = slot<Boolean>()
        val listOfResponse = arrayListOf<Boolean>()

        every { verifierRepository.getCertificateFetchStatus()}.returns(response)

        every { mockObserver.onChanged(capture(slot)) } answers {
            listOfResponse.add(slot.captured)
        }

        viewModel = FirstViewModel(verifierRepository, preferences)

        viewModel.fetchStatus.observeForever(mockObserver)

        assertEquals(false, listOfResponse[0])
    }

}