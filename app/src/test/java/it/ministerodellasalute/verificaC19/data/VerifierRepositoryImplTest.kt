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
 *  Created by climent on 6/15/21 1:53 PM
 */

package it.ministerodellasalute.verificaC19.data

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import it.ministerodellasalute.verificaC19.data.local.AppDatabase
import it.ministerodellasalute.verificaC19.data.local.Preferences
import it.ministerodellasalute.verificaC19.data.remote.ApiService
import it.ministerodellasalute.verificaC19.di.DispatcherProvider
import it.ministerodellasalute.verificaC19.security.KeyStoreCryptor
import it.ministerodellasalute.verificaC19.utils.MainCoroutineScopeRule
import it.ministerodellasalute.verificaC19.utils.mock.ServiceMocks
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import retrofit2.Response

class VerifierRepositoryImplTest{

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineScopeRule: MainCoroutineScopeRule = MainCoroutineScopeRule()

    @RelaxedMockK
    private lateinit var apiService: ApiService

    @RelaxedMockK
    private lateinit var preferences: Preferences

    @RelaxedMockK
    private lateinit var appDatabase: AppDatabase

    @RelaxedMockK
    private lateinit var keyStoreCryptor: KeyStoreCryptor

    @RelaxedMockK
    private val dispatcherProvider: DispatcherProvider = mockk()

    private lateinit var repository: VerifierRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every{dispatcherProvider.getIO()}.returns(mainCoroutineScopeRule.testDispatcher)

        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0

        repository = VerifierRepositoryImpl(apiService, preferences, appDatabase, keyStoreCryptor, dispatcherProvider)
    }

    @Test
    fun `test syncData`() = mainCoroutineScopeRule.runBlockingTest{
        val verificationRulesResponse = ServiceMocks.getVerificationRulesStringResponse()
        val kidResponse = ServiceMocks.getQrCodeKid()

        val mockObserver = mockk<Observer<Boolean>>()
        val slot = slot<Boolean>()
        val listOfResponse = arrayListOf<Boolean>()

        coEvery{apiService.getValidationRules()}.returns(Response.success(verificationRulesResponse.toResponseBody()))
        coEvery{apiService.getCertStatus()}.returns(Response.success(listOf(kidResponse)))

        every { mockObserver.onChanged(capture(slot)) } answers {
            listOfResponse.add(slot.captured)
        }

        repository.getCertificateFetchStatus().observeForever(mockObserver)

        repository.syncData()

        Assert.assertEquals(true, listOfResponse[0])
        Assert.assertEquals(false, listOfResponse[1])
        verify { preferences.validationRulesJson = verificationRulesResponse }
        verify { preferences.resumeToken }
    }

}