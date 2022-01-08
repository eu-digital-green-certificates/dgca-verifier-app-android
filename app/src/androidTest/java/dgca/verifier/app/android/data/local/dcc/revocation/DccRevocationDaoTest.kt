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
import dcc.app.revocation.data.DccRevocationHashType
import dcc.app.revocation.data.DccRevocationKidMetadata
import dgca.verifier.app.android.data.local.AppDatabase
import dgca.verifier.app.android.data.local.dcc.revocation.data.DccRevocationPartitionLocal
import dgca.verifier.app.android.data.local.dcc.revocation.data.toLocal
import dgca.verifier.app.android.utils.sha256
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
internal class DccRevocationDaoTest {
    private lateinit var dccRevocationDao: DccRevocationDao
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
        dccRevocationDao = db.dccRevocationPartitionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun dccRevocationKidMetadataTest() {
        val kid = "a0a0a0"

        assertTrue(dccRevocationDao.getDccRevocationKidMetadataList(kid).isEmpty())

        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = DccRevocationHashType.SIGNATURE,
            mode = "mode",
            tag = "tag"
        )

        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())


        // Test inserting item with hashType = Signature
        var list = dccRevocationDao.getDccRevocationKidMetadataList(kid = kid)

        assertEquals(1, list.size)
        assertEquals(dccRevocationKidSignatureMetadata.toLocal().copy(kidMetadataId = 1), list[0])


        // Test inserting the same element twice
        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())

        list = dccRevocationDao.getDccRevocationKidMetadataList(kid = kid)

        assertEquals(1, list.size)
        assertEquals(dccRevocationKidSignatureMetadata.toLocal().copy(kidMetadataId = 2), list[0])

        // Test inserting item with other hashType = Country Code UCI
        val dccRevocationKidCountryCodeUciMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = DccRevocationHashType.COUNTRYCODEUCI,
            mode = "mode",
            tag = "tag"
        )

        dccRevocationDao.insert(dccRevocationKidCountryCodeUciMetadata.toLocal())

        list = dccRevocationDao.getDccRevocationKidMetadataList(kid = kid)

        assertEquals(2, list.size)
        assertTrue(list.contains(dccRevocationKidCountryCodeUciMetadata.toLocal().copy(kidMetadataId = 3)))


        // Test inserting item with other hashType = UCI
        val dccRevocationKidUciMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = DccRevocationHashType.UCI,
            mode = "mode",
            tag = "tag"
        )

        dccRevocationDao.insert(dccRevocationKidUciMetadata.toLocal())

        list = dccRevocationDao.getDccRevocationKidMetadataList(kid = kid)

        assertEquals(3, list.size)
        assertTrue(list.contains(dccRevocationKidUciMetadata.toLocal().copy(kidMetadataId = 4)))


        // Test deleting items by kid
        dccRevocationDao.deleteDccRevocationKidMetadataListBy(kid = kid)

        list = dccRevocationDao.getDccRevocationKidMetadataList(kid = kid)
        assertTrue(list.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val revocationPartition0: DccRevocationPartitionLocal =
            fetchPartition(REVOCATION_PARTITION_0)
        dccRevocationDao.insert(revocationPartition0)

        val actualRevocationPartition0 = dccRevocationDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertEquals(revocationPartition0, actualRevocationPartition0)

        val revokedDccBlob = revocationPartition0.revokedDccsBlob
        val newSha256 = "new".sha256()
        val newRevokedDccBlob = revokedDccBlob + newSha256 + String.format("%019d", System.currentTimeMillis())

        dccRevocationDao.insert(revocationPartition0.copy(revokedDccsBlob = newRevokedDccBlob))

        val actualNewRevocationPartition0 = dccRevocationDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertEquals(newRevokedDccBlob, actualNewRevocationPartition0?.revokedDccsBlob)

        dccRevocationDao.delete(revocationPartition0)

        val actualEmptyRevocationPartitionLocal = dccRevocationDao.get(
            kid = revocationPartition0.kid,
            firstDccHashByte = revocationPartition0.firstDccHashByte,
            secondDccHashByte = revocationPartition0.secondDccHashByte
        )

        assertNull(actualEmptyRevocationPartitionLocal)
    }
}
