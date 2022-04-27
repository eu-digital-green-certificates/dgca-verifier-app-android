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
 *  Created by mykhailo.nester on 23/03/2022, 21:32
 */

package dgca.verifier.app.android.vc.di

import android.content.Context
import com.android.app.vc.BuildConfig
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.vc.data.remote.VcApiService
import dgca.verifier.app.android.vc.network.HeaderInterceptor
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

private const val CONNECT_TIMEOUT = 30L

@InstallIn(SingletonComponent::class)
@Module
object VcNetworkModule {

    @Singleton
    @Provides
    @VerifiableCredentials
    internal fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    @VerifiableCredentials
    internal fun provideOkhttpClient(@VerifiableCredentials cache: Cache): OkHttpClient {
        val httpClient = getHttpClient(cache).apply {
            addInterceptor(HeaderInterceptor())
        }
        addLogging(httpClient)

        return httpClient.build()
    }

    @Singleton
    @Provides
    @VerifiableCredentials
    internal fun provideRetrofit(
        converterFactory: Converter.Factory,
        @VerifiableCredentials okHttpClient: Provider<OkHttpClient>
    ): Retrofit =
        createRetrofit(converterFactory, okHttpClient)

    @Singleton
    @Provides
    internal fun provideApiService(@VerifiableCredentials retrofit: Retrofit): VcApiService =
        retrofit.create(VcApiService::class.java)

    private fun getHttpClient(cache: Cache): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)

    private fun addLogging(httpClient: OkHttpClient.Builder) {
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
        }
    }

    @Singleton
    @Provides
    @VerifiableCredentials
    internal fun provideObjectMapper(): ObjectMapper = ObjectMapper().apply { findAndRegisterModules() }

    @Singleton
    @Provides
    @VerifiableCredentials
    internal fun provideConverterFactory(objectMapper: ObjectMapper): Converter.Factory =
        JacksonConverterFactory.create(objectMapper)

    private fun createRetrofit(converterFactory: Converter.Factory, okHttpClient: Provider<OkHttpClient>): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .baseUrl(BuildConfig.VC_SERVICE_HOST)
            .callFactory { okHttpClient.get().newCall(it) }
            .build()
    }
}

@PublishedApi
internal inline fun Retrofit.Builder.callFactory(
    crossinline body: (Request) -> Call
) = callFactory(object : Call.Factory {
    override fun newCall(request: Request): Call = body(request)
})

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VerifiableCredentials