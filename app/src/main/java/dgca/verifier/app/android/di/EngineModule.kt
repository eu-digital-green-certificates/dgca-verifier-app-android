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
 *  Created by Mykhailo Nester on 4/23/21 9:48 AM
 */

package dgca.verifier.app.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.data.remote.ApiService
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import dgca.verifier.app.engine.JsonLogicValidator
import dgca.verifier.app.engine.data.source.DefaultRulesRepository
import dgca.verifier.app.engine.data.source.RulesRepository
import dgca.verifier.app.engine.data.source.local.DefaultRulesLocalDataSource
import dgca.verifier.app.engine.data.source.local.RulesDao
import dgca.verifier.app.engine.data.source.local.RulesDatabase
import dgca.verifier.app.engine.data.source.local.RulesLocalDataSource
import dgca.verifier.app.engine.data.source.remote.DefaultRulesRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.RulesApiService
import dgca.verifier.app.engine.data.source.remote.RulesRemoteDataSource
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provide json cert logic validation dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
object EngineModule {

    @Singleton
    @Provides
    fun provideRulesDb(@ApplicationContext context: Context): RulesDatabase =
        Room.databaseBuilder(context, RulesDatabase::class.java, "rule-db").build()

    @Singleton
    @Provides
    fun provideRulesDao(rulesDatabase: RulesDatabase): RulesDao = rulesDatabase.rulesDao()

    @Singleton
    @Provides
    fun provideRulesLocalDataSource(rulesDao: RulesDao): RulesLocalDataSource =
        DefaultRulesLocalDataSource(rulesDao)

    @Singleton
    @Provides
    internal fun provideRulesApiService(retrofit: Retrofit): RulesApiService {
        return retrofit.create(RulesApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRulesRemoteDataSource(rulesApiService: RulesApiService): RulesRemoteDataSource =
        DefaultRulesRemoteDataSource(rulesApiService)

    @Singleton
    @Provides
    fun provideRulesRepository(
        remoteDataSource: RulesRemoteDataSource,
        localDataSource: RulesLocalDataSource
    ): RulesRepository = DefaultRulesRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideCryptoService(): JsonLogicValidator = DefaultJsonLogicValidator()

    @Singleton
    @Provides
    fun provideCertLogicEngine(jsonLogicValidator: JsonLogicValidator): CertLogicEngine =
        DefaultCertLogicEngine(jsonLogicValidator)
}