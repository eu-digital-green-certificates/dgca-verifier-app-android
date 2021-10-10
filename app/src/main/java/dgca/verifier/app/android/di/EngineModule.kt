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
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.data.local.countries.CountriesDao
import dgca.verifier.app.android.data.local.countries.DefaultCountriesLocalDataSource
import dgca.verifier.app.android.data.local.rules.DefaultRulesLocalDataSource
import dgca.verifier.app.android.data.local.rules.EngineDatabase
import dgca.verifier.app.android.data.local.rules.RulesDao
import dgca.verifier.app.android.data.local.valuesets.DefaultValueSetsLocalDataSource
import dgca.verifier.app.android.data.local.valuesets.ValueSetsDao
import dgca.verifier.app.android.data.remote.countries.CountriesApiService
import dgca.verifier.app.android.data.remote.countries.DefaultCountriesRemoteDataSource
import dgca.verifier.app.android.data.remote.rules.DefaultRulesRemoteDataSource
import dgca.verifier.app.android.data.remote.rules.RulesApiService
import dgca.verifier.app.android.data.remote.valuesets.DefaultValueSetsRemoteDataSource
import dgca.verifier.app.android.data.remote.valuesets.ValueSetsApiService
import dgca.verifier.app.decoder.JSON_SCHEMA_V1
import dgca.verifier.app.engine.*
import dgca.verifier.app.engine.data.source.countries.CountriesRepository
import dgca.verifier.app.engine.data.source.countries.DefaultCountriesRepository
import dgca.verifier.app.engine.data.source.local.countries.CountriesLocalDataSource
import dgca.verifier.app.engine.data.source.local.rules.RulesLocalDataSource
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsLocalDataSource
import dgca.verifier.app.engine.data.source.remote.countries.CountriesRemoteDataSrouce
import dgca.verifier.app.engine.data.source.remote.rules.RulesRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.rules.DefaultRulesRepository
import dgca.verifier.app.engine.data.source.rules.RulesRepository
import dgca.verifier.app.engine.data.source.valuesets.DefaultValueSetsRepository
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import dgca.verifier.app.engine.domain.rules.DefaultGetRulesUseCase
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provide json cert logic validation dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
object EngineModule {

    // Dependencies for rules.

    @Singleton
    @Provides
    fun provideRulesDb(@ApplicationContext context: Context): EngineDatabase =
        Room.databaseBuilder(context, EngineDatabase::class.java, "engine-db")
            .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideRulesDao(engineDatabase: EngineDatabase): RulesDao = engineDatabase.rulesDao()

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

    // Dependencies for engine.

    @Singleton
    @Provides
    fun provideCryptoService(): JsonLogicValidator = DefaultJsonLogicValidator()

    @Singleton
    @Provides
    fun provideAffectedFieldsDataRetriever(objectMapper: ObjectMapper): AffectedFieldsDataRetriever =
        DefaultAffectedFieldsDataRetriever(objectMapper.readTree(JSON_SCHEMA_V1), objectMapper)

    @Singleton
    @Provides
    fun provideCertLogicEngine(
        affectedFieldsDataRetriever: AffectedFieldsDataRetriever,
        jsonLogicValidator: JsonLogicValidator
    ): CertLogicEngine =
        DefaultCertLogicEngine(affectedFieldsDataRetriever, jsonLogicValidator)

    // Dependencies for countries.

    @Singleton
    @Provides
    fun provideCountriesDao(engineDatabase: EngineDatabase): CountriesDao =
        engineDatabase.countriesDao()

    @Singleton
    @Provides
    fun provideCountriesLocalDataSource(countriesDao: CountriesDao): CountriesLocalDataSource =
        DefaultCountriesLocalDataSource(countriesDao)

    @Singleton
    @Provides
    internal fun provideCountriesApiService(retrofit: Retrofit): CountriesApiService {
        return retrofit.create(CountriesApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCountriesRemoteDataSource(countriesApiService: CountriesApiService): CountriesRemoteDataSrouce =
        DefaultCountriesRemoteDataSource(countriesApiService)

    @Singleton
    @Provides
    fun provideCountriesRepository(
        remoteDataSource: CountriesRemoteDataSrouce,
        localDataSource: CountriesLocalDataSource
    ): CountriesRepository = DefaultCountriesRepository(remoteDataSource, localDataSource)

    // Dependencies for value sets.

    @Singleton
    @Provides
    fun provideValueSetsDao(engineDatabase: EngineDatabase): ValueSetsDao =
        engineDatabase.valueSetsDao()

    @Singleton
    @Provides
    fun provideValueSetsLocalDataSource(dao: ValueSetsDao): ValueSetsLocalDataSource =
        DefaultValueSetsLocalDataSource(dao)

    @Singleton
    @Provides
    internal fun provideValueSetsApiService(retrofit: Retrofit): ValueSetsApiService {
        return retrofit.create(ValueSetsApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideValueSetsiRemoteDataSource(apiService: ValueSetsApiService): ValueSetsRemoteDataSource =
        DefaultValueSetsRemoteDataSource(apiService)

    @Singleton
    @Provides
    fun provideValueSetsRepository(
        remoteDataSource: ValueSetsRemoteDataSource,
        localDataSource: ValueSetsLocalDataSource
    ): ValueSetsRepository = DefaultValueSetsRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideGetRulesUseCase(
        rulesRepository: RulesRepository
    ): GetRulesUseCase = DefaultGetRulesUseCase(rulesRepository)
}