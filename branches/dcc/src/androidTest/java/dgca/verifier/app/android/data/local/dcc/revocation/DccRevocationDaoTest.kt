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
 *  Created by osarapulov on 3/18/22, 10:15 AM
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
import dcc.app.revocation.di.BASE_URL
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.*
import dcc.app.revocation.domain.usacase.IsDccRevokedUseCase
import dcc.app.revocation.repository.RevocationRepositoryImpl
import dcc.app.revocation.validation.bloom.BloomFilterImpl
import dcc.app.revocation.validation.hash.PartialVariableHashFilter
import dcc.app.revocation.validation.hash.PartitionOffset
import dgca.verifier.app.android.dcc.data.local.AppDatabase
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.DccRevocationDao
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.DccRevocationLocalDataSourceImpl
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.mapper.fromLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.mapper.toLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationKidMetadataLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationPartitionLocal
import dgca.verifier.app.android.dcc.data.local.dcc.revocation.model.DccRevocationSliceLocal
import dgca.verifier.app.android.dcc.utils.sha256
import dgca.verifier.app.decoder.toBase64
import dgca.verifier.app.engine.UTC_ZONE_ID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toHexString
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.assertEquals
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
        const val REVOCATION_PARTITION_0 = "$REVOCATION_PATH/revocation_partition_0.json"

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
        revocationService =
            Retrofit.Builder().baseUrl(BASE_URL).build().create(RevocationService::class.java)
        revocationPreferences = RevocationPreferencesImpl(context)
        revocationRepository = RevocationRepositoryImpl(
            revocationService,
            revocationPreferences,
            dccRevocationLocalDataSource
        )
        errorHandler = GeneralErrorHandlerImpl()
        testCoroutineDispatcher = Dispatchers.Main
        isDccRevokedUseCase =
            IsDccRevokedUseCase(revocationRepository, testCoroutineDispatcher, errorHandler)
        return
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun generateUniqueKids(amount: Int): Array<String> {
        val kids = mutableListOf<String>()
        for (c in 0 until amount) {
            val kid = Random.nextBytes(9).toBase64()
            kids.add(kid)
        }
        return kids.toTypedArray()
    }

    // Prefixes represent 3 first characters of hashes to be stored in partition.
    private fun generateHashPrefixes(mode: DccRevocationMode): Array<String> {
        val pointModeHashPrefix = ""
        return when (mode) {
            DccRevocationMode.POINT -> arrayOf(pointModeHashPrefix)
            DccRevocationMode.VECTOR -> {
                val vectorModePrefixes = mutableSetOf<String>()
                var x = 0
                while (x < hexCharsAmount && vectorModePrefixes.size < 16) {
                    val prefix = "${hexChars[x++]}"
                    vectorModePrefixes.add(prefix)
                }
                vectorModePrefixes.toTypedArray()
            }
            DccRevocationMode.COORDINATE -> {
                val coordinateModePrefixes = mutableSetOf<String>()
                var x = 0
                while (x < hexCharsAmount && coordinateModePrefixes.size < 256) {
                    var y = 0
                    while (y < hexCharsAmount) {
                        val prefix = "${hexChars[x]}${hexChars[y++]}"
                        coordinateModePrefixes.add(prefix)
                    }
                    x++
                }
                return coordinateModePrefixes.toTypedArray()
            }
            else -> throw IllegalArgumentException()
        }
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
        val amountOfKids = 1
        val wantedAmountOfHashes = 100_000
        val mode = DccRevocationMode.POINT
        // Amount of partitions
        val chunksPerPartition = 16
        val kids = generateUniqueKids(amountOfKids)

        val amountOfSlicesPerChunk = 10
        val amountOfHashesPerSlice = when (mode) {
            DccRevocationMode.POINT -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * amountOfKids)
            DccRevocationMode.VECTOR -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 16 * amountOfKids)
            DccRevocationMode.COORDINATE -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 256 * amountOfKids)
            else -> throw IllegalArgumentException()
        }

        var shouldSkipHashesGeneration = false
        kids.forEach { kid ->
            val kidMetadata = generateKidMetadata(kid, mode)
            dccRevocationDao.insert(kidMetadata)
        }

        val hashes = mutableListOf<String>()
        val dummyhashes = mutableListOf<String>()
        val partitions = mutableListOf<DccRevocationPartitionLocal>()

        val modeHashPrefixes = generateHashPrefixes(mode)
        modeHashPrefixes.forEachIndexed { index, hashPrefix ->
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
            var counter = 0
            val bloomFilter = BloomFilterImpl(amountOfHashesPerSlice, 0.00000000001f)
            chunks.forEach { (cid, slices) ->
                slices.forEach { (expirationTime, slice) ->
                    val hashStart = hashPrefix + cid

                    if (counter < 2) {
                        for (i in 0..amountOfHashesPerSlice) {
                            val hash = if (shouldSkipHashesGeneration) {
                                Random.nextInt(Integer.MAX_VALUE).toHexString()
                            } else {
                                shouldSkipHashesGeneration = true
                                System.currentTimeMillis().toString().sha256()
                                    .replaceRange(0, hashStart.length, hashStart)
                            }

                            bloomFilter.add(hash.toByteArray())
                            hashes.add(hash)

                            if (shouldSkipHashesGeneration) {
                                dummyhashes.add(hash)
                            }
                        }
                    } else {
                        if (hashes.size < 1_000_000) // hash bookkeeping needs a lot of memory, 1M is enough to simulate random accessing
                            hashes.addAll(dummyhashes)
                    }

                    counter++

                    val content = ByteArrayOutputStream().use {
                        bloomFilter.writeTo(it)
                        it.toByteArray()
                    }
                    println("MYTAG BF bytes amount: ${content.size}, num of elements: $amountOfHashesPerSlice")
                    val sliceLocal = DccRevocationSliceLocal(
                        sid = slice.hash,
                        kid = kid,
                        x = x,
                        y = y,
                        cid = cid,
                        type = DccSliceType.values().first { it.tag == slice.type },
                        version = slice.version,
                        expires = expirationTime,
                        content = content
                    )
                    //slicesList.add(sliceLocal)
                    dccRevocationDao.insert(sliceLocal)
                }
            }
        }

        dccRevocationDao.insertList(partitions)

        var shouldNotContain = true
        val results = mutableListOf<Long>()
        val r = Random(hashes.size)
        for (x in 0..100) {
            val testS = System.currentTimeMillis()

            if (x == 0) {
                shouldNotContain = shouldNotContain and isDccRevokedUseCase.execute(
                    DccRevokationDataHolder(
                        kid = kids.first(),
                        hashes.first(),
                        hashes.first(),
                        hashes.first(),
                    )
                )!!
            } else {
                val idx = r.nextInt(hashes.size)
                shouldNotContain = shouldNotContain and isDccRevokedUseCase.execute(
                    DccRevokationDataHolder(
                        kid = kids.first(),
                        hashes[idx],
                        hashes[idx],
                        hashes[idx]
                    )
                )!!
            }
            val testE = System.currentTimeMillis()
            results.add(testE - testS)
        }

        println("MYTAG Test times: Min: ${results.minOrNull()} Max: ${results.maxOrNull()} Avg:  ${results.sum() / results.count()}")
        return@runBlocking
    }

    @Test
    fun loadTestHashVariable() = runBlocking {
        val amountOfKids = 1
        val wantedAmountOfHashes = 100_000
        val mode = DccRevocationMode.COORDINATE
        // Amount of partitions
        val chunksPerPartition = 16
        val kids = generateUniqueKids(amountOfKids)

        val amountOfSlicesPerChunk = 10
        val amountOfHashesPerSlice = when (mode) {
            DccRevocationMode.POINT -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * amountOfKids)
            DccRevocationMode.VECTOR -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 16 * amountOfKids)
            DccRevocationMode.COORDINATE -> wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 256 * amountOfKids)
            else -> throw IllegalArgumentException()
        }

        var shouldSkipHashesGeneration = false
        kids.forEach { kid ->
            val kidMetadata = generateKidMetadata(kid, mode)
            dccRevocationDao.insert(kidMetadata)
        }

        val hashes = mutableListOf<String>()
        val partitions = mutableListOf<DccRevocationPartitionLocal>()

        val modeHashPrefixes = generateHashPrefixes(mode)
        modeHashPrefixes.forEachIndexed { index, hashPrefix ->
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
            var counter = 0
            val offset = when (mode) {
                DccRevocationMode.POINT -> PartitionOffset.POINT
                DccRevocationMode.VECTOR -> PartitionOffset.VECTOR
                DccRevocationMode.COORDINATE -> PartitionOffset.COORDINATE
                DccRevocationMode.UNKNOWN -> PartitionOffset.POINT
            }

            chunks.forEach { (cid, slices) ->
                slices.forEach { (expirationTime, slice) ->
                    val hashFilter =
                        PartialVariableHashFilter(4, offset, amountOfHashesPerSlice, 0.00000000001f)
                    val hashStart = hashPrefix + cid

//                    if (counter < 2) {
                    for (i in 0 until amountOfHashesPerSlice) {
                        val hash = if (shouldSkipHashesGeneration) {
                            i.toHexString().sha256()
                        } else {
                            shouldSkipHashesGeneration = true
                            System.currentTimeMillis().toString().sha256()
                                .replaceRange(0, hashStart.length, hashStart)
                        }

                        hashFilter.add(hash.toByteArray())
                        hashes.add(hash)

//                            if (shouldSkipHashesGeneration) {
//                                dummyhashes.add(hash)
//                            }
                    }
//                    } else {
//                        if (hashes.size < 1_000_000) // hash bookkeeping needs a lot of memory, 1M is enough to simulate random accessing
//                            hashes.addAll(dummyhashes)
//                    }
                    counter++

                    val content = hashFilter.writeTo()
                    println("MYTAG BF bytes amount: ${content.size}, num of elements: $amountOfHashesPerSlice")
                    val sliceLocal = DccRevocationSliceLocal(
                        sid = slice.hash,
                        kid = kid,
                        x = x,
                        y = y,
                        cid = cid,
                        type = DccSliceType.VARHASHLIST,
                        version = slice.version,
                        expires = expirationTime,
                        content = content
                    )
                    dccRevocationDao.insert(sliceLocal)
                }
            }
        }

        dccRevocationDao.insertList(partitions)

        var shouldNotContain = true
        val results = mutableListOf<Long>()
        val r = Random(hashes.size)
        for (x in 0..100) {
            val testS = System.currentTimeMillis()

            if (x == 0) {
                shouldNotContain = shouldNotContain and isDccRevokedUseCase.execute(
                    DccRevokationDataHolder(
                        kid = kids.first(),
                        hashes.first(),
                        hashes.first(),
                        hashes.first(),
                    )
                )!!
            } else {
                val idx = r.nextInt(hashes.size)
                shouldNotContain = shouldNotContain and isDccRevokedUseCase.execute(
                    DccRevokationDataHolder(
                        kid = kids.first(),
                        hashes[idx],
                        hashes[idx],
                        hashes[idx]
                    )
                )!!
            }
            val testE = System.currentTimeMillis()
            results.add(testE - testS)
        }

        println("MYTAG Test times: Min: ${results.minOrNull()} Max: ${results.maxOrNull()} Avg:  ${results.sum() / results.count()}")
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

        val xDccPartition = nullDccPartition.copy(id = "x", x = hashBytes[0].toInt().toChar())
        dccRevocationDao.insert(xDccPartition.toLocal())

        val xyDccPartition = xDccPartition.copy(id = "xy", y = hashBytes[1].toInt().toChar())
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
