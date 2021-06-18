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
 *  Created by climent on 6/14/21 3:25 PM
 */

package it.ministerodellasalute.verificaC19.di

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import it.ministerodellasalute.verificaC19.security.DefaultKeyStoreCryptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import dagger.Lazy
import it.ministerodellasalute.verificaC19.data.remote.ApiService
import retrofit2.Retrofit
import java.io.File

class NetworkModuleTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var cache: Cache

    @RelaxedMockK
    lateinit var okHttpClient: Lazy<OkHttpClient>

    private lateinit var module: NetworkModule

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        module = NetworkModule
    }

    @Test
    fun `test provideCache dependency`() {
        every { context.cacheDir }.returns(File(""))

        MatcherAssert.assertThat(
            "provideCache",
            module.provideCache(context),
            CoreMatchers.instanceOf(Cache::class.java)
        )
    }

    @Test
    fun `test provideOkhttpClient dependency`() {
        MatcherAssert.assertThat(
            "provideOkhttpClient",
            module.provideOkhttpClient(cache),
            CoreMatchers.instanceOf(OkHttpClient::class.java)
        )
    }

    @Test
    fun `test provideRetrofit dependency`() {
        MatcherAssert.assertThat(
            "provideRetrofit",
            module.provideRetrofit(okHttpClient),
            CoreMatchers.instanceOf(Retrofit::class.java)
        )
    }

}