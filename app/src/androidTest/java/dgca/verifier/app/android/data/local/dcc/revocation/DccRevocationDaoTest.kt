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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dcc.app.revocation.data.GeneralErrorHandlerImpl
import dcc.app.revocation.data.RevocationPreferences
import dcc.app.revocation.data.RevocationPreferencesImpl
import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dcc.app.revocation.data.network.RevocationService
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.*
import dcc.app.revocation.domain.toSha256HexString
import dcc.app.revocation.domain.usacase.IsDccRevokedUseCase
import dcc.app.revocation.repository.RevocationRepositoryImpl
import dcc.app.revocation.validation.BloomFilterImpl
import dgca.verifier.app.android.data.local.AppDatabase
import dgca.verifier.app.android.data.local.dcc.revocation.mapper.fromLocal
import dgca.verifier.app.android.data.local.dcc.revocation.mapper.toLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationHashListSliceLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.data.local.dcc.revocation.model.DccRevocationSliceLocal
import dgca.verifier.app.android.di.BASE_URL
import dgca.verifier.app.android.utils.sha256
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.engine.UTC_ZONE_ID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toHexString
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min
import kotlin.random.Random

data class Slice(
    val hash: String,
    val type: String = DccSliceType.BF.tag,
    val version: String = "1.0"
)

@RunWith(AndroidJUnit4::class)
internal class DccRevocationDaoTest {
    private lateinit var dccRevocationDao: DccRevocationDao
    private lateinit var db: AppDatabase
    private val objectMapper = ObjectMapper().apply { this.findAndRegisterModules() }

    @ExperimentalCoroutinesApi
    private lateinit var testCoroutineDispatcher: CoroutineDispatcher
    private lateinit var errorHandler: ErrorHandler
    private lateinit var revocationService: RevocationService
    private lateinit var revocationPreferences: RevocationPreferences
    private lateinit var dccRevocationLocalDataSource: DccRevocationLocalDataSource
    private lateinit var revocationRepository: RevocationRepository
    private lateinit var isDccRevokedUseCase: IsDccRevokedUseCase

    companion object {
        private const val REVOCATION_PATH = "revocation"
        const val REVOCATION_PARTITION_0 = "${REVOCATION_PATH}/revocation_partition_0.json"

        private val hexChars =
            arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        private val hexCharsAmount = hexChars.size
        private val amountOfUniqueHexCharsPairs = hexCharsAmount * hexCharsAmount
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
        dccRevocationLocalDataSource = DccRevocationLocalDataSourceImpl(dccRevocationDao)
        revocationService = Retrofit.Builder().baseUrl(BASE_URL).build().create(RevocationService::class.java)
        revocationPreferences = RevocationPreferencesImpl(context)
        revocationRepository = RevocationRepositoryImpl(
            revocationService,
            revocationPreferences,
            dccRevocationLocalDataSource
        )
        errorHandler = GeneralErrorHandlerImpl()
        testCoroutineDispatcher = Dispatchers.Main
        isDccRevokedUseCase = IsDccRevokedUseCase(revocationRepository, testCoroutineDispatcher, errorHandler)
        return
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun generateUniqueKids(amount: Int): Map<DccRevocationMode, Array<String>> {
        if (amount <= 0) return emptyMap()

        val pointModeKid = Random.nextBytes(9).toBase64()
        val modeKids = mutableMapOf(DccRevocationMode.POINT to arrayOf(pointModeKid))

        val amountOfVectorAndCoordinateKids = amount - 1
        val amountOfCoordinateKids = amountOfVectorAndCoordinateKids / 2
        val amountOfVectorKids = amountOfVectorAndCoordinateKids - amountOfCoordinateKids

        val vectorModeKids = mutableSetOf<String>()
        while (vectorModeKids.size < hexCharsAmount && vectorModeKids.size < amountOfVectorKids) {
            val kid = Random.nextBytes(9).toBase64()
            vectorModeKids.add(kid)
        }
        if (vectorModeKids.isNotEmpty()) {
            modeKids[DccRevocationMode.VECTOR] = vectorModeKids.toTypedArray()
        }

        val coordinateModeKids = mutableSetOf<String>()
        while (coordinateModeKids.size < amountOfUniqueHexCharsPairs && coordinateModeKids.size < amountOfCoordinateKids) {
            val kid = Random.nextBytes(9).toBase64()
            coordinateModeKids.add(kid)
        }
        if (coordinateModeKids.isNotEmpty()) {
            modeKids[DccRevocationMode.COORDINATE] = coordinateModeKids.toTypedArray()
        }

        return modeKids
    }

    // Prefixes represent 3 first characters of hashes to be stored in partition.
    private fun generateHashPrefixes(amount: Int): Map<DccRevocationMode, Array<String>> {
        if (amount <= 0) return emptyMap()

        val pointModeHashPrefix = ""
        val modeHashPrefixes = mutableMapOf(DccRevocationMode.POINT to arrayOf(pointModeHashPrefix))

        val vectorModePrefixes = mutableSetOf<String>()
        var x = 0
        while (x < hexCharsAmount && vectorModePrefixes.size + 1 < amount) {
            val prefix = "${hexChars[x++]}"
            vectorModePrefixes.add(prefix)
        }
        modeHashPrefixes[DccRevocationMode.VECTOR] = vectorModePrefixes.toTypedArray()

        val coordinateModePrefixes = mutableSetOf<String>()
        x = 0
        while (x < hexCharsAmount && coordinateModePrefixes.size + vectorModePrefixes.size + 1 < amount) {
            var y = 0
            while (y < hexCharsAmount && coordinateModePrefixes.size + vectorModePrefixes.size + 1 < amount) {
                val prefix = "${hexChars[x]}${hexChars[y++]}"
                coordinateModePrefixes.add(prefix)
            }
            x++
        }
        modeHashPrefixes[DccRevocationMode.COORDINATE] = coordinateModePrefixes.toTypedArray()

        return modeHashPrefixes
    }

    private fun generateChunks(
        chunksAmount: Int,
        slicesAmount: Int
    ): Map<String, Map<ZonedDateTime, Slice>> {
        val chunks = mutableMapOf<String, Map<ZonedDateTime, Slice>>()
        for (i in 0 until chunksAmount) {
            val slices = mutableMapOf<ZonedDateTime, Slice>()
            for (j in 0 until slicesAmount) {
                slices[ZonedDateTime.now().plusSeconds(j.toLong())] = Slice(
                    hash = UUID.randomUUID().toString()
                )
            }
            chunks["${hexChars[i % hexChars.size]}"] = slices
        }
        return chunks
    }

    private fun generateKidMetadata(
        kid: String,
        mode: DccRevocationMode
    ): DccRevocationKidMetadataLocal {
        return DccRevocationKidMetadataLocal(
            kid = kid,
            hashType = setOf(
                DccRevocationHashType.COUNTRYCODEUCI,
                DccRevocationHashType.SIGNATURE,
                DccRevocationHashType.UCI
            ),
            mode = mode,
            expires = ZonedDateTime.now().plusYears(1),
            lastUpdated = ZonedDateTime.now()
        )
    }

    @Test
    fun loadTest() = runBlocking {
        val testStartTime = System.currentTimeMillis()

        val amountOfKids = 1
        // Amount of partitions
        val amountOfHashPrefixes = min(256, 1)
        val chunksPerPartition = min(16, Int.MAX_VALUE)
        val wantedAmountOfHashes = 10_000
        val minAmountOfHashes = amountOfHashPrefixes * chunksPerPartition
        val approximateAmountOfHashes =
            if (wantedAmountOfHashes < minAmountOfHashes) minAmountOfHashes else wantedAmountOfHashes
        val hashesPerPrefix = approximateAmountOfHashes / amountOfHashPrefixes
        val hashesPerChunk = hashesPerPrefix / chunksPerPartition
        val wantedAmountOfSlicesPerChunk = 10
        val amountOfSlicesPerChunk =
            if (wantedAmountOfSlicesPerChunk > hashesPerChunk) hashesPerChunk else wantedAmountOfSlicesPerChunk
        val amountOfHashesPerSlice =
            approximateAmountOfHashes / (amountOfHashPrefixes * chunksPerPartition * amountOfSlicesPerChunk)
        val modeKids = generateUniqueKids(amountOfKids)

        var shouldSkipHashesGeneration = false

        modeKids.forEach { (mode, kids) ->
            kids.forEach { kid ->
                val kidMetadata = generateKidMetadata(kid, mode)
                dccRevocationDao.insert(kidMetadata)
            }
        }

        val hashes = mutableListOf<String>()
        val partitions = mutableListOf<DccRevocationPartitionLocal>()
        val slicesList = mutableListOf<DccRevocationSliceLocal>()

        val modeHashPrefixes = generateHashPrefixes(amountOfHashPrefixes)
        DccRevocationMode.values().forEach { mode ->
            val kids: Array<String>? = modeKids[mode]
            val hashPrefixes: Array<String>? = modeHashPrefixes[mode]

            if (kids != null && hashPrefixes != null) {
                hashPrefixes.forEachIndexed { index, hashPrefix ->
                    val kidIndex = index % kids.size
                    val kid = kids[kidIndex]

                    val (x, y) = when (mode) {
                        DccRevocationMode.POINT -> Pair(null, null)
                        DccRevocationMode.VECTOR -> Pair(hashPrefix[0], null)
                        DccRevocationMode.COORDINATE -> Pair(hashPrefix[0], hashPrefix[1])
                        DccRevocationMode.UNKNOWN -> throw IllegalStateException()
                    }

                    val chunks = generateChunks(chunksPerPartition, amountOfSlicesPerChunk)
                    val chunksString = jacksonObjectMapper().writeValueAsString(chunks)

                    val partition = DccRevocationPartitionLocal(
                        id = "$index",
                        kid = kid,
                        x = x,
                        y = y,
                        expires = ZonedDateTime.now(),
                        chunks = chunksString
                    )
                    partitions.add(partition)

                    val bloomFilter = BloomFilterImpl(amountOfHashesPerSlice, 0.00000000001F)
                    chunks.forEach { (cid, slices) ->
                        slices.forEach { (expirationTime, slice) ->
                            val hashStart = hashPrefix + cid

                            for (i in 0..amountOfHashesPerSlice) {
                                val hash = if (shouldSkipHashesGeneration) {
                                    Random.nextInt(Integer.MAX_VALUE).toHexString()
                                } else {
                                    shouldSkipHashesGeneration = true
                                    System.currentTimeMillis().toString().toByteArray().toSha256HexString()
                                        .replaceRange(0, hashStart.length, hashStart)
                                }

                                bloomFilter.add(hash.toByteArray())
                                hashes.add(hash)
                            }
//                            val content = ByteArray(0)
                            val content = ByteArrayOutputStream().use {
                                bloomFilter.writeTo(it)
                                it.toByteArray()
                            }
                            println("MYTAG BF bytes amount: ${content.size}, num of elements: ${amountOfHashesPerSlice}")
                            val sliceLocal = DccRevocationSliceLocal(
                                sid = slice.hash,
                                kid = kid,
                                x = x,
                                y = y,
                                cid = cid,
                                type = DccSliceType.values().first() { it.tag == slice.type },
                                version = slice.version,
                                expires = expirationTime,
                                content = content
                            )
                            slicesList.add(sliceLocal)
                        }
                    }
                }
            }
        }

        dccRevocationDao.insertList(partitions)
        dccRevocationDao.insertSlicesList(slicesList)
        val searchExistingTimeStart = System.currentTimeMillis()
        val res = isDccRevokedUseCase.execute(
            DccRevokationDataHolder(
                kid = modeKids[DccRevocationMode.POINT]!!.first(),
                uvciSha256 = hashes.first(),
                coUvciSha256 = hashes.first(),
                signatureSha256 = hashes.first()
            )
        )
        val searchExistingTimeEnd = System.currentTimeMillis()
        println("MYTAG Existing hash search time: ${searchExistingTimeEnd - searchExistingTimeStart}")

        assertTrue(res!!)

        val shouldNotContain = isDccRevokedUseCase.execute(
            DccRevokationDataHolder(
                kid = modeKids[DccRevocationMode.POINT]!!.first(),
                uvciSha256 = hashes.first().replaceRange(0, 1, "+"),
                coUvciSha256 = hashes.first().replaceRange(0, 1, "+"),
                signatureSha256 = hashes.first().replaceRange(0, 1, "+")
            )
        )
        assertFalse(shouldNotContain!!)

        val testEndTime = System.currentTimeMillis()
        println("MYTAG Test time: ${testEndTime - testStartTime}")
        return@runBlocking
    }

    @Test
    fun loadHashListTest() = runBlocking {
        val testStartTime = System.currentTimeMillis()

        val amountOfKids = 1
        // Amount of partitions
        val amountOfHashPrefixes = min(256, 1)
        val chunksPerPartition = min(16, Int.MAX_VALUE)
        val wantedAmountOfHashes = 10_000
        val minAmountOfHashes = amountOfHashPrefixes * chunksPerPartition
        val approximateAmountOfHashes =
            if (wantedAmountOfHashes < minAmountOfHashes) minAmountOfHashes else wantedAmountOfHashes
        val hashesPerPrefix = approximateAmountOfHashes / amountOfHashPrefixes
        val hashesPerChunk = hashesPerPrefix / chunksPerPartition
        val wantedAmountOfSlicesPerChunk = 10
        val amountOfSlicesPerChunk =
            if (wantedAmountOfSlicesPerChunk > hashesPerChunk) hashesPerChunk else wantedAmountOfSlicesPerChunk
        val amountOfHashesPerSlice =
            approximateAmountOfHashes / (amountOfHashPrefixes * chunksPerPartition * amountOfSlicesPerChunk)
        val modeKids = generateUniqueKids(amountOfKids)

        var shouldSkipHashesGeneration = false

        modeKids.forEach { (mode, kids) ->
            kids.forEach { kid ->
                val kidMetadata = generateKidMetadata(kid, mode)
                dccRevocationDao.insert(kidMetadata)
            }
        }

        val hashes = mutableListOf<String>()
        val hashBytes = mutableListOf<ByteArray>()
        val partitions = mutableListOf<DccRevocationPartitionLocal>()
        val slicesList = mutableListOf<DccRevocationSliceLocal>()

        val modeHashPrefixes = generateHashPrefixes(amountOfHashPrefixes)
        DccRevocationMode.values().forEach { mode ->
            val kids: Array<String>? = modeKids[mode]
            val hashPrefixes: Array<String>? = modeHashPrefixes[mode]

            if (kids != null && hashPrefixes != null) {
                hashPrefixes.forEachIndexed { index, hashPrefix ->
                    val kidIndex = index % kids.size
                    val kid = kids[kidIndex]

                    val (x, y) = when (mode) {
                        DccRevocationMode.POINT -> Pair(null, null)
                        DccRevocationMode.VECTOR -> Pair(hashPrefix[0], null)
                        DccRevocationMode.COORDINATE -> Pair(hashPrefix[0], hashPrefix[1])
                        DccRevocationMode.UNKNOWN -> throw IllegalStateException()
                    }

                    val chunks = generateChunks(chunksPerPartition, amountOfSlicesPerChunk)
                    val chunksString = jacksonObjectMapper().writeValueAsString(chunks)

                    val partition = DccRevocationPartitionLocal(
                        id = "$index",
                        kid = kid,
                        x = x,
                        y = y,
                        expires = ZonedDateTime.now(),
                        chunks = chunksString
                    )
                    partitions.add(partition)

                    chunks.forEach { (cid, slices) ->
                        slices.forEach { (expirationTime, slice) ->
                            val hashStart = hashPrefix + cid

                            val hashListByteArray = mutableListOf<ByteArray>()
                            for (i in 0..amountOfHashesPerSlice) {
                                val hash = if (shouldSkipHashesGeneration) {
                                    byteArrayOf(i.toByte(), i.toByte())
                                } else {
                                    shouldSkipHashesGeneration = true
                                    "12".toByteArray()
                                }

                                hashListByteArray.add(hash)
                                hashBytes.add(hash)
                                hashes.add(hash[0].toString() + hash[1].toString())
                            }

                            println("MYTAG BF bytes amount: ${hashListByteArray.size}, num of elements: $amountOfHashesPerSlice")
                            val sliceLocal = DccRevocationSliceLocal(
                                sid = slice.hash,
                                kid = kid,
                                x = x,
                                y = y,
                                cid = cid,
                                type = DccSliceType.HASH,
                                version = slice.version,
                                expires = expirationTime,
                                content = byteArrayOf()
                            )
                            dccRevocationDao.insert(sliceLocal)

                            val hashListSlices = mutableListOf<DccRevocationHashListSliceLocal>()
                            hashListByteArray.forEach {
                                hashListSlices.add(
                                    DccRevocationHashListSliceLocal(
                                        sid = slice.hash,
                                        x = x,
                                        y = y,
                                        hash = it
                                    )
                                )
                            }
                            dccRevocationDao.insertHashListSlices(hashListSlices)
                        }
                    }
                }
            }
        }

        dccRevocationDao.insertList(partitions)

        val searchExistingTimeStart = System.currentTimeMillis()
        val res = isDccRevokedUseCase.execute(
            DccRevokationDataHolder(
                kid = modeKids[DccRevocationMode.POINT]!!.first(),
                uvciSha256 = "12",
                coUvciSha256 = "12",
                signatureSha256 = "12"
            )
        )
        val searchExistingTimeEnd = System.currentTimeMillis()
        println("MYTAG Existing hash search time: ${searchExistingTimeEnd - searchExistingTimeStart}")

        assertTrue(res!!)

        val shouldNotContain = isDccRevokedUseCase.execute(
            DccRevokationDataHolder(
                kid = modeKids[DccRevocationMode.POINT]!!.first(),
                uvciSha256 = hashes.first().replaceRange(0, 1, "+"),
                coUvciSha256 = hashes.first().replaceRange(0, 1, "+"),
                signatureSha256 = hashes.first().replaceRange(0, 1, "+")
            )
        )
        assertFalse(shouldNotContain!!)

        val testEndTime = System.currentTimeMillis()
        println("MYTAG Test time: ${testEndTime - testStartTime}")
        return@runBlocking
    }

    @Test
    fun dccRevocationPartitionTest() = runBlocking {
        val kid = "a0a0a0"

        // Insert kid metadata
        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = setOf(DccRevocationHashType.COUNTRYCODEUCI),
            mode = DccRevocationMode.POINT,
            expires = ZonedDateTime.now().plusYears(1),
            lastUpdated = ZonedDateTime.now()
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
            expires = ZonedDateTime.now().plusYears(1).withZoneSameInstant(UTC_ZONE_ID)
        )

        dccRevocationDao.insert(nullDccPartition.toLocal())

        val xDccPartition = nullDccPartition.copy(id = "x", x = hashBytes[0].toChar())
        dccRevocationDao.insert(xDccPartition.toLocal())

        val xyDccPartition = xDccPartition.copy(id = "xy", y = hashBytes[1].toChar())
        dccRevocationDao.insert(xyDccPartition.toLocal())

        assertEquals(
            nullDccPartition, dccRevocationDao.getDccRevocationPartition(
                kid = kid,
                x = nullDccPartition.x,
                y = nullDccPartition.y,
            )?.fromLocal()
        )
        assertEquals(
            xDccPartition, dccRevocationDao.getDccRevocationPartition(
                kid = kid,
                x = xDccPartition.x,
                y = xDccPartition.y,
            )?.fromLocal()
        )
        assertEquals(
            xyDccPartition, dccRevocationDao.getDccRevocationPartition(
                kid = kid,
                x = xyDccPartition.x,
                y = xyDccPartition.y,
            )?.fromLocal()
        )
    }
}
