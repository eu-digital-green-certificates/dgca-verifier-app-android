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
import dcc.app.revocation.domain.model.*
import dgca.verifier.app.android.data.local.AppDatabase
import dgca.verifier.app.android.data.local.dcc.revocation.mapper.fromLocal
import dgca.verifier.app.android.data.local.dcc.revocation.mapper.toLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.utils.sha256
import kotlinx.coroutines.runBlocking
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

//    @Test
//    fun dccRevocationKidMetadataTest() {
//        val kid = "a0a0a0"
//
//        assertTrue(dccRevocationDao.getDccRevocationKidMetadataListBy(kid).isEmpty())
//
//        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
//            kid = kid,
//            hashType = DccRevocationHashType.SIGNATURE,
//            mode = DccRevocationMode.POINT,
//            tag = "tag"
//        )
//
//        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())
//
//
//        // Test inserting item with hashType = Signature
//        var list = dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
//
//        assertEquals(1, list.size)
//        assertEquals(dccRevocationKidSignatureMetadata.toLocal().copy(kidMetadataId = 1), list[0])
//
//
//        // Test inserting the same element twice
//        val dccRevocationKidSignatureMetadataNew = DccRevocationKidMetadata(
//            kid = kid,
//            hashType = DccRevocationHashType.SIGNATURE,
//            mode = DccRevocationMode.POINT,
//            tag = "newTag"
//        )
//
//        dccRevocationDao.insert(dccRevocationKidSignatureMetadataNew.toLocal())
//
//        list = dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
//
//        assertEquals(1, list.size)
//        assertEquals(
//            dccRevocationKidSignatureMetadataNew.toLocal().copy(kidMetadataId = 2),
//            list[0]
//        )
//
//
//        // Test inserting item with other hashType = Country Code UCI
//        val dccRevocationKidCountryCodeUciMetadata = DccRevocationKidMetadata(
//            kid = kid,
//            hashType = DccRevocationHashType.COUNTRYCODEUCI,
//            mode = DccRevocationMode.POINT,
//            tag = "tag"
//        )
//
//        dccRevocationDao.insert(dccRevocationKidCountryCodeUciMetadata.toLocal())
//
//        list = dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
//
//        assertEquals(2, list.size)
//        assertTrue(
//            list.contains(
//                dccRevocationKidCountryCodeUciMetadata.toLocal().copy(kidMetadataId = 3)
//            )
//        )
//
//
//        // Test inserting item with other hashType = UCI
//        val dccRevocationKidUciMetadata = DccRevocationKidMetadata(
//            kid = kid,
//            hashType = DccRevocationHashType.UCI,
//            mode = DccRevocationMode.POINT,
//            tag = "tag"
//        )
//
//        dccRevocationDao.insert(dccRevocationKidUciMetadata.toLocal())
//
//        list = dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
//
//        assertEquals(3, list.size)
//        assertTrue(list.contains(dccRevocationKidUciMetadata.toLocal().copy(kidMetadataId = 4)))
//
//
//        // Test deleting items by kid
//        dccRevocationDao.deleteDccRevocationKidMetadataListBy(kid = kid)
//
//        list = dccRevocationDao.getDccRevocationKidMetadataListBy(kid = kid)
//        assertTrue(list.isEmpty())
//    }
//
//    @Test(expected = SQLiteConstraintException::class)
//    fun dccRevocationPartitionCantInsertTest() {
//        val kid = "a0a0a0"
//
//        val dccRevocationPartition = DccRevocationPartition(
//            kid = kid,
//            x = null,
//            y = null,
//            pid = "pid",
//            hashType = DccRevocationHashType.SIGNATURE,
//            version = "version",
//            expiration = ZonedDateTime.now(),
//            chunks = "chunks"
//        )
//
//        dccRevocationDao.insert(dccRevocationPartition.toLocal())
//    }

    @Test
    fun dccRevocationPartitionTest() = runBlocking {
        val kid = "a0a0a0"

        // Insert kid metadata
        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = setOf(DccRevocationHashType.COUNTRYCODEUCI),
            mode = DccRevocationMode.POINT,
            expires = System.currentTimeMillis() + System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis().toString()
        )

        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())

        val hash = "".sha256()
        val hashBytes = hash.toByteArray()

        val nullDccPartition = DccRevocationPartition(
            kid = kid,
            x = null,
            y = null,
            chunks = "chunks",
            id = "null",
            z = null,
            expires = 0
        )

        dccRevocationDao.insert(nullDccPartition.toLocal())

        val xDccPartition = nullDccPartition.copy(id = "x", x = hashBytes[0].toChar())
        dccRevocationDao.insert(xDccPartition.toLocal())

        val xyDccPartition = xDccPartition.copy(id = "xy", y = hashBytes[1].toChar())
        dccRevocationDao.insert(xyDccPartition.toLocal())

        val xyzDccPartition = xyDccPartition.copy(id = "xyz", z = hashBytes[2].toChar())
        dccRevocationDao.insert(xyzDccPartition.toLocal())

        assertEquals(4, dccRevocationDao.getDccRevocationPartitionListBy(kid = kid).size)
        assertEquals(nullDccPartition, dccRevocationDao.getDccRevocationPartition(
            kid = kid,
            x = nullDccPartition.x,
            y = nullDccPartition.y,
            z = nullDccPartition.z
        )?.fromLocal())
        assertEquals(xDccPartition, dccRevocationDao.getDccRevocationPartition(
            kid = kid,
            x = xDccPartition.x,
            y = xDccPartition.y,
            z = xDccPartition.z
        )?.fromLocal())
        assertEquals(xyDccPartition, dccRevocationDao.getDccRevocationPartition(
            kid = kid,
            x = xyDccPartition.x,
            y = xyDccPartition.y,
            z = xyDccPartition.z
        )?.fromLocal())
        assertEquals(xyzDccPartition, dccRevocationDao.getDccRevocationPartition(
            kid = kid,
            x = xyzDccPartition.x,
            y = xyzDccPartition.y,
            z = xyzDccPartition.z
        )?.fromLocal())
    }

//
//    @Test(expected = SQLiteConstraintException::class)
//    fun dccRevocationChunkCantInsertTest() {
//        val kid = "a0a0a0"
//
//        val dccRevocationChunk = DccRevocationChunk(
//            kid = kid,
//            x = null,
//            y = null,
//            pid = "pid",
//            version = "version",
//            expiration = ZonedDateTime.now(),
//            cid = "cid",
//            type = DccChunkType.HASH,
//            section = "section",
//            content = "content"
//        )
//
//        dccRevocationDao.insert(dccRevocationChunk.toLocal())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun dccRevocationChunkTest() {
//        val kid = "a0a0a0"
//
//
//        // Insert kid metadata
//        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
//            kid = kid,
//            hashType = DccRevocationHashType.SIGNATURE,
//            mode = DccRevocationMode.POINT,
//            tag = "tag"
//        )
//
//        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())
//
//        val hash = "".sha256()
//        val hashBytes = hash.toByteArray()
//        val x = hashBytes[0]
//        val y = hashBytes[1]
//        val pid = "pid"
//        val version = "version"
//        val expiration = ZonedDateTime.now(UTC_ZONE_ID)
//
//
//        // Insert partition
//        val dccRevocationPartition = DccRevocationPartition(
//            kid = kid,
//            x = x,
//            y = y,
//            pid = pid,
//            hashType = DccRevocationHashType.SIGNATURE,
//            version = version,
//            expiration = expiration,
//            chunks = "chunks"
//        )
//
//        dccRevocationDao.insert(dccRevocationPartition.toLocal())
//
//
//        // Insert chunk
//        val dccRevocationChunk = DccRevocationChunk(
//            kid = kid,
//            x = x,
//            y = y,
//            cid = "cid",
//            pid = pid,
//            type = DccChunkType.HASH,
//            version = version,
//            expiration = expiration,
//            section = "",
//            content = ""
//        )
//
//        dccRevocationDao.insert(dccRevocationChunk.toLocal())
//
//        var list = dccRevocationDao.getDccRevocationChunkList(
//            kid = dccRevocationChunk.kid
//        )
//
//        assertEquals(1, list.size)
//        assertTrue(list.contains(dccRevocationChunk.toLocal().copy(chunkId = 1)))
//
//        // Insert the same chunk
//        val dccRevocationNewChunk = DccRevocationChunk(
//            kid = kid,
//            x = x,
//            y = y,
//            cid = "cid",
//            pid = pid,
//            type = DccChunkType.HASH,
//            version = version,
//            expiration = expiration,
//            section = "new",
//            content = "new"
//        )
//
//        dccRevocationDao.insert(dccRevocationNewChunk.toLocal())
//
//        list = dccRevocationDao.getDccRevocationChunkList(
//            kid = dccRevocationChunk.kid
//        )
//
//        assertEquals(1, list.size)
//        assertEquals(dccRevocationNewChunk.toLocal().copy(chunkId = 2), list.first())
//
//        // Insert another chunk
//        val dccRevocationAnotherChunk = DccRevocationChunk(
//            kid = kid,
//            x = x,
//            y = y,
//            cid = "another_cid",
//            pid = pid,
//            type = DccChunkType.HASH,
//            version = version,
//            expiration = expiration,
//            section = "new",
//            content = "new"
//        )
//
//        dccRevocationDao.insert(dccRevocationAnotherChunk.toLocal())
//
//        list = dccRevocationDao.getDccRevocationChunkList(
//            kid = dccRevocationChunk.kid
//        )
//
//        assertEquals(2, list.size)
//        assertTrue(list.contains(dccRevocationAnotherChunk.toLocal().copy(chunkId = 3)))
//
//        // Remove kid
//        dccRevocationDao.deleteDccRevocationKidMetadataListBy(kid = kid)
//
//        list = dccRevocationDao.getDccRevocationChunkList(
//            kid = dccRevocationChunk.kid
//        )
//
//        assertTrue(list.isEmpty())
//    }
}
