/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 8:29 AM
 */

package dgca.verifier.app.android.di

import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.DefaultProtocolHandler
import dgca.verifier.app.android.ProtocolHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ActivityModule {

    @Singleton
    @Provides
    fun provideProcessor(@ProcessorMarker processors: Set<@JvmSuppressWildcards Processor>): ProtocolHandler =
        DefaultProtocolHandler(processors)
}
