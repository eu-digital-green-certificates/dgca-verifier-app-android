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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import dgca.verifier.app.engine.JsonLogicValidator
import dgca.verifier.app.engine.data.source.DefaultRulesRepository
import dgca.verifier.app.engine.data.source.RulesRepository
import dgca.verifier.app.engine.data.source.local.DefaultRulesLocalDataSource
import dgca.verifier.app.engine.data.source.local.RulesLocalDataSource
import dgca.verifier.app.engine.data.source.remote.DefaultRulesRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.RulesRemoteDataSource
import javax.inject.Singleton

/**
 * Provide json cert logic validation dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
object EngineModule {

    @Singleton
    @Provides
    fun provideRulesLocalDataSource(): RulesLocalDataSource = DefaultRulesLocalDataSource()

    @Singleton
    @Provides
    fun provideRulesRemoteDataSource(): RulesRemoteDataSource = DefaultRulesRemoteDataSource()

    @Singleton
    @Provides
    fun provideRulesRepository(
        remoteDataSource: RulesRemoteDataSource,
        localDataSource: RulesLocalDataSource
    ): RulesRepository = DefaultRulesRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideCryptoService(): JsonLogicValidator = DefaultJsonLogicValidator()
}