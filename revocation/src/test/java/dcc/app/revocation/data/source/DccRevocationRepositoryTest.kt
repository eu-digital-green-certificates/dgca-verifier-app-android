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

import dcc.app.revocation.data.DccRevocationEntry
import dcc.app.revocation.data.DccRevocationPartition
import dcc.app.revocation.data.source.local.DccRevocationLocalDataSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.time.ZonedDateTime


@RunWith(MockitoJUnitRunner::class)
class DccRevocationRepositoryTest {
    private lateinit var dccRevocationRepository: DccRevocationRepository

    private val testKid = "a1b2c3"
    private val testDccRevocationHash =
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    private val anotherTestDccRevocationHashSamePartition =
        "e3864eb339b0e1f6e00d75293a8840abf069a2c0fe82e6e53af6ac099793c1d5"

    @Mock
    lateinit var dccRevocationLocalDataSource: DccRevocationLocalDataSource

    @Before
    fun setUp() {
        dccRevocationRepository = DccRevocationRepositoryImpl(dccRevocationLocalDataSource)
    }

    @Test
    fun testAddFields() {
        val testDccExpirationDate = ZonedDateTime.now()
        val expectedBlob = testDccRevocationHash + String.format(
            "%019d",
            testDccExpirationDate.toInstant().toEpochMilli()
        )

        dccRevocationRepository.add(testKid, testDccRevocationHash, testDccExpirationDate)

        doReturn(
            DccRevocationPartition(
                testKid,
                testDccRevocationHash[0],
                testDccRevocationHash[1],
                testDccRevocationHash + String.format(
                    "%019d",
                    testDccExpirationDate.minusDays(1).toInstant().toEpochMilli()
                )
            )
        ).`when`(dccRevocationLocalDataSource)
            .getBy(eq(testKid), eq(testDccRevocationHash[0]), eq(testDccRevocationHash[1]))

        dccRevocationRepository.add(testKid, testDccRevocationHash, testDccExpirationDate)

        verify(dccRevocationLocalDataSource, times(2)).addOrUpdate(
            DccRevocationPartition(
                testKid, testDccRevocationHash[0], testDccRevocationHash[1],
                expectedBlob
            )
        )
    }

    @Test
    fun testAddEntry() {
        val testDccExpirationDate = ZonedDateTime.now()

        dccRevocationRepository.add(
            testKid,
            DccRevocationEntry(testDccRevocationHash, testDccExpirationDate)
        )

        verify(dccRevocationLocalDataSource).addOrUpdate(
            DccRevocationPartition(
                testKid, testDccRevocationHash[0], testDccRevocationHash[1],
                testDccRevocationHash + String.format(
                    "%019d",
                    testDccExpirationDate.toInstant().toEpochMilli()
                )
            )
        )
    }

    @Test
    fun testContains() {
        doReturn(
            DccRevocationPartition(
                testKid,
                testDccRevocationHash[0],
                testDccRevocationHash[1],
                testDccRevocationHash + String.format(
                    "%019d",
                    ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli()
                )
            )
        ).`when`(dccRevocationLocalDataSource)
            .getBy(eq(testKid), eq(testDccRevocationHash[0]), eq(testDccRevocationHash[1]))

        assertFalse(dccRevocationRepository.contains(testKid, testDccRevocationHash))

        doReturn(
            DccRevocationPartition(
                testKid,
                testDccRevocationHash[0],
                testDccRevocationHash[1],
                testDccRevocationHash + String.format(
                    "%019d",
                    ZonedDateTime.now().plusDays(1).toInstant().toEpochMilli()
                )
            )
        ).`when`(dccRevocationLocalDataSource)
            .getBy(eq(testKid), eq(testDccRevocationHash[0]), eq(testDccRevocationHash[1]))

        assertTrue(dccRevocationRepository.contains(testKid, testDccRevocationHash))

        assertFalse(dccRevocationRepository.contains(testKid, "random_string"))
    }

    @Test
    fun testRemove() {
        val testDccExpirationTimestampString = String.format("%019d", System.currentTimeMillis())
        val testOriginalBlob = testDccRevocationHash + testDccExpirationTimestampString +
                anotherTestDccRevocationHashSamePartition + testDccExpirationTimestampString

        doReturn(
            DccRevocationPartition(
                testKid,
                testDccRevocationHash[0],
                testDccRevocationHash[1],
                testOriginalBlob
            )
        ).`when`(dccRevocationLocalDataSource)
            .getBy(eq(testKid), eq(testDccRevocationHash[0]), eq(testDccRevocationHash[1]))

        dccRevocationRepository.remove(testKid, testDccRevocationHash)

        val testNewBlob =
            anotherTestDccRevocationHashSamePartition + testDccExpirationTimestampString

        verify(dccRevocationLocalDataSource).addOrUpdate(
            DccRevocationPartition(
                testKid, testDccRevocationHash[0], testDccRevocationHash[1], testNewBlob
            )
        )

        doReturn(
            DccRevocationPartition(
                testKid,
                anotherTestDccRevocationHashSamePartition[0],
                anotherTestDccRevocationHashSamePartition[1],
                testNewBlob
            )
        ).`when`(dccRevocationLocalDataSource)
            .getBy(
                eq(testKid),
                eq(anotherTestDccRevocationHashSamePartition[0]),
                eq(anotherTestDccRevocationHashSamePartition[1])
            )

        dccRevocationRepository.remove(testKid, anotherTestDccRevocationHashSamePartition)

        val argument: ArgumentCaptor<DccRevocationPartition> =
            ArgumentCaptor.forClass(DccRevocationPartition::class.java)

        verify(dccRevocationLocalDataSource).remove(
            capture(argument)
        )
        assertEquals(testKid, argument.value.kid)
    }
}
