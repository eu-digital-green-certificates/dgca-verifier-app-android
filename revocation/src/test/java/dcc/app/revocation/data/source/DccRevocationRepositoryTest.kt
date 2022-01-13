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
 *  Created by osarapulov on 1/4/22, 7:14 AM
 */

package dcc.app.revocation.data.source

import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dcc.app.revocation.data.local.DccRevocationRepository
import dcc.app.revocation.data.local.DccRevocationRepositoryImpl
import dcc.app.revocation.domain.model.DccRevocationHashType
import dcc.app.revocation.domain.model.DccRevocationKidMetadata
import dcc.app.revocation.domain.model.DccRevocationMode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class DccRevocationRepositoryTest {
    private lateinit var dccRevocationRepository: DccRevocationRepository

    @Mock
    lateinit var dccRevocationLocalDataSource: DccRevocationLocalDataSource

    @Before
    fun setUp() {
        dccRevocationRepository = DccRevocationRepositoryImpl(dccRevocationLocalDataSource)
    }

    @Test
    fun addOrUpdateTest() {
        val dccRevocationKidMetadata = DccRevocationKidMetadata(
            kid = "kid",
            hashType = DccRevocationHashType.SIGNATURE,
            mode = DccRevocationMode.POINT,
            expires = "2020",
            lastUpdated = "lastUpdate"
        )

        dccRevocationRepository.addOrUpdate(dccRevocationKidMetadata)

        verify(dccRevocationLocalDataSource).addOrUpdate(dccRevocationKidMetadata)
    }

    @Test
    fun removeDccRevocationKidMetadataByTest() {
        val kid = "kid"

        dccRevocationRepository.removeDccRevocationKidMetadataBy(kid)

        verify(dccRevocationLocalDataSource).removeDccRevocationKidMetadataBy(kid)
    }
}
