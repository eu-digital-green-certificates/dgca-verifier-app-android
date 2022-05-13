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

package dgca.verifier.app.android.diia.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dgca.verifier.app.android.diia.data.local.AppDatabase
import dgca.verifier.app.android.diia.data.local.diia.revocation.DccRevocationDao
import dgca.verifier.app.android.diia.data.local.diia.revocation.DccRevocationLocalDataSourceImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalDataSourceModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "key-db")
            .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideRevokedDccDao(appDatabase: AppDatabase): DccRevocationDao =
        appDatabase.dccRevocationPartitionDao()

    @Singleton
    @Provides
    fun provideRevokedLocalDataSource(revocationDccRevocationDao: DccRevocationDao): DccRevocationLocalDataSource =
        DccRevocationLocalDataSourceImpl(revocationDccRevocationDao)

}
