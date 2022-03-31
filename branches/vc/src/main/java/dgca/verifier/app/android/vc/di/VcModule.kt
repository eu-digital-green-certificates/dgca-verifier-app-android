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
 *  Created by mykhailo.nester on 23/03/2022, 22:35
 */

package dgca.verifier.app.android.vc.di

import android.content.Context
import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dgca.verifier.app.android.vc.VcProcessor
import dgca.verifier.app.android.vc.data.DefaultJwsTokenParser
import dgca.verifier.app.android.vc.data.JwsTokenParser
import dgca.verifier.app.android.vc.data.TrustListRepository
import dgca.verifier.app.android.vc.data.TrustListRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class VcModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideVcProcessor(@ApplicationContext context: Context): Processor = VcProcessor(context)

    @Singleton
    @Provides
    fun provideTrustListRepository(): TrustListRepository = TrustListRepositoryImpl()

    @Singleton
    @Provides
    fun provideJwsTokenParser(): JwsTokenParser = DefaultJwsTokenParser()
}