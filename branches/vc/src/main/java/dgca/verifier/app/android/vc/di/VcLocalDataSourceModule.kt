/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 3/17/22, 2:47 PM
 */

package dgca.verifier.app.android.vc.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.vc.data.local.JwkDao
import dgca.verifier.app.android.vc.data.local.VcDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object VcLocalDataSourceModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): VcDatabase =
        Room.databaseBuilder(context, VcDatabase::class.java, "vc_certificate_issuers_db")
            .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideJwkDao(database: VcDatabase): JwkDao = database.jwkDao()
}
