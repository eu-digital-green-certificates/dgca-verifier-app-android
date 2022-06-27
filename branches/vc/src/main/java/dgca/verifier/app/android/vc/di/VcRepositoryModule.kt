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
 *  Created by mykhailo.nester on 13/04/2022, 12:49
 */

package dgca.verifier.app.android.vc.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.vc.data.VcRepository
import dgca.verifier.app.android.vc.data.VcRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class VcRepositoryModule {

    @Singleton
    @Binds
    abstract fun bindVcRepository(repository: VcRepositoryImpl): VcRepository
}