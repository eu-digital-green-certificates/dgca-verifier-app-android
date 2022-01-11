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
 *  Created by osarapulov on 1/4/22, 6:47 AM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import dcc.app.revocation.data.DccRevocationHashType
import dcc.app.revocation.data.DccRevocationKidMetadata
import dcc.app.revocation.data.DccRevocationMode
import dcc.app.revocation.data.DccRevocationPartition
import dcc.app.revocation.data.source.local.DccRevocationLocalDataSource
import dgca.verifier.app.android.data.local.dcc.revocation.data.toLocal
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.time.ZonedDateTime

@RunWith(MockitoJUnitRunner::class)
class DccRevocationLocalDataSourceTest {
    private lateinit var dccRevocationLocalDataSource: DccRevocationLocalDataSource

    private val testDccRevocationKidMetadata = DccRevocationKidMetadata(
        kid = "a1b2c3",
        hashType = DccRevocationHashType.SIGNATURE,
        mode = DccRevocationMode.POINT,
        tag = "tag"
    )

    private val testDccRevocationPartition = DccRevocationPartition(
        kid = "a1b2c3",
        x = 'a'.toByte(),
        y = '1'.toByte(),
        pid = "pid",
        hashType = DccRevocationHashType.SIGNATURE,
        version = "version",
        expiration = ZonedDateTime.now(),
        chunks = ""
    )

    @Mock
    lateinit var dccRevocationDao: DccRevocationDao

    @Before
    fun setUp() {
        dccRevocationLocalDataSource = DccRevocationLocalDataSourceImpl(dccRevocationDao)
    }

    @Test
    fun testAddOrUpdateDccRevocationKidMetadata() {
        dccRevocationLocalDataSource.addOrUpdate(testDccRevocationKidMetadata)

        verify(dccRevocationDao).insert(testDccRevocationKidMetadata.toLocal())
    }

    @Test
    fun testRemoveDccRevocationKidMetadata() {
        dccRevocationLocalDataSource.removeDccRevocationKidMetadataBy(testDccRevocationKidMetadata.kid)

        verify(dccRevocationDao).deleteDccRevocationKidMetadataListBy(eq(testDccRevocationKidMetadata.kid))
    }

    @Test
    fun testAddOrUpdate() {
        dccRevocationLocalDataSource.addOrUpdate(testDccRevocationPartition)

        verify(dccRevocationDao).insert(testDccRevocationPartition.toLocal())
    }

    @Test
    fun testGetBy() {
        doReturn(listOf(testDccRevocationPartition.toLocal())).`when`(dccRevocationDao).getDccRevocationPartitionListBy(
            eq(testDccRevocationPartition.kid)
        )

        val actual = dccRevocationLocalDataSource.getDccRevocationPartitionListBy(
            testDccRevocationPartition.kid
        )

        verify(dccRevocationDao).getDccRevocationPartitionListBy(
            eq(testDccRevocationPartition.kid)
        )
        assertEquals(listOf(testDccRevocationPartition), actual)
    }

    @Test
    fun testRemove() {
        dccRevocationLocalDataSource.removeDccRevocationPartitionBy(testDccRevocationPartition.pid)

        verify(dccRevocationDao).deleteDccRevocationPartitionBy(partitionId = testDccRevocationPartition.pid)
    }
}
