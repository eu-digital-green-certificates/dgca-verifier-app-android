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
 *  Created by osarapulov on 1/3/22, 10:55 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.android.data.local.AppDatabase
import dgca.verifier.app.android.utils.sha256
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
internal class DccRevocationPartitionDaoTest {
    private lateinit var dccRevocationPartitionDao: DccRevocationPartitionDao
    private lateinit var db: AppDatabase
    private val objectMapper = ObjectMapper().apply { this.findAndRegisterModules() }

    companion object {
        private const val REVOCATION_PATH = "revocation"
        const val REVOCATION_PARTITION_0 = "${REVOCATION_PATH}/revocation_partition_0.json"
    }

    private fun fetchPartition(fileName: String): DccRevocationPartitionLocal {
        val inputStream: InputStream =
            javaClass.classLoader!!.getResourceAsStream(fileName)
        val ruleJson = IOUtils.toString(inputStream, Charset.defaultCharset())
        return objectMapper.readValue(ruleJson, DccRevocationPartitionLocal::class.java)
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        dccRevocationPartitionDao = db.dccRevocationPartitionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val revocationPartition0: DccRevocationPartitionLocal =
            fetchPartition(REVOCATION_PARTITION_0)
        dccRevocationPartitionDao.insert(revocationPartition0)

        val actualRevocationPartition0 = dccRevocationPartitionDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertEquals(revocationPartition0, actualRevocationPartition0)

        val revokedDccBlob = revocationPartition0.revokedDccsBlob
        val newSha256 = "new".sha256()
        val newRevokedDccBlob = revokedDccBlob + newSha256 + String.format("%019d", System.currentTimeMillis())

        dccRevocationPartitionDao.insert(revocationPartition0.copy(revokedDccsBlob = newRevokedDccBlob))

        val actualNewRevocationPartition0 = dccRevocationPartitionDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertEquals(newRevokedDccBlob, actualNewRevocationPartition0?.revokedDccsBlob)

        dccRevocationPartitionDao.delete(revocationPartition0)

        val actualEmptyRevocationPartitionLocal = dccRevocationPartitionDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertNull(actualEmptyRevocationPartitionLocal)
    }
}
