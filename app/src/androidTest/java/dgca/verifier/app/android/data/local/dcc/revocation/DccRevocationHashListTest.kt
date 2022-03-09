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
 *  Created by mykhailo.nester on 15/02/2022, 16:37
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dcc.app.revocation.domain.model.DccSliceType
import dgca.verifier.app.android.data.local.AppDatabase
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationHashListSliceLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationSliceLocal
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.io.IOException
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class DccRevocationHashListTest {

    private lateinit var dccRevocationDao: DccRevocationDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        dccRevocationDao = db.dccRevocationPartitionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun addHashListSlice_constraintFailedTest() = runBlocking {
        val hash = byteArrayOf(2.toByte(), 1.toByte())
        val hashListSlice = DccRevocationHashListSliceLocal(
            id = 1,
            sid = "sidTest",
            x = 'x',
            y = 'y',
            hash = hash
        )
        val hashListSlices = listOf(hashListSlice)

        try {
            dccRevocationDao.insertHashListSlices(hashListSlices)
            Assert.fail()
        } catch (ex: SQLiteConstraintException) {
            Timber.d("Exception expected")
        }
    }

    @Test
    fun getHashListSlice_successTest() = runBlocking {
        // Given
        val sliceLocal = DccRevocationSliceLocal(
            sid = "sidTest",
            kid = "kid",
            x = 'x',
            y = 'y',
            cid = "c",
            type = DccSliceType.HASH,
            version = "1.0",
            expires = ZonedDateTime.now(),
            content = byteArrayOf()
        )
        dccRevocationDao.insert(sliceLocal)
        val hash = byteArrayOf(2.toByte(), 1.toByte())
        val hashListSlice = DccRevocationHashListSliceLocal(
            id = 1,
            sid = "sidTest",
            x = 'x',
            y = 'y',
            hash = hash
        )
        val hashListSlices = listOf(hashListSlice)
        dccRevocationDao.insertHashListSlices(hashListSlices)

        // When
        val result = dccRevocationDao.getHashListSlice(setOf("sidTest"), 'x', 'y', hash)

        // Then
        assertThat(result, equalTo(hashListSlice))
    }

    @Test
    fun deleteSlice_shouldDeleteHashList_successTest() = runBlocking {
        // Given
        val sliceLocal = DccRevocationSliceLocal(
            sid = "sidTest",
            kid = "kid",
            x = 'x',
            y = 'y',
            cid = "c",
            type = DccSliceType.HASH,
            version = "1.0",
            expires = ZonedDateTime.now(),
            content = byteArrayOf()
        )
        dccRevocationDao.insert(sliceLocal)
        val hash = byteArrayOf(2.toByte(), 1.toByte())
        val hashListSlice = DccRevocationHashListSliceLocal(
            id = 1,
            sid = "sidTest",
            x = 'x',
            y = 'y',
            hash = hash
        )
        val hashListSlices = listOf(hashListSlice)
        dccRevocationDao.insertHashListSlices(hashListSlices)

        // When
        dccRevocationDao.deleteSlice("sidTest")

        // Then
        val result = dccRevocationDao.getHashListSlice(setOf("sidTest"), 'x', 'y', hash)
        assertThat(result, nullValue())
    }
}