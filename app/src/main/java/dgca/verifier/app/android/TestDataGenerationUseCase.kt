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
 *  Created by mykhailo.nester on 28/01/2022, 17:32
 */

package dgca.verifier.app.android

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.model.*
import dcc.app.revocation.domain.usacase.BaseUseCase
import dcc.app.revocation.validation.BloomFilterImpl
import dgca.verifier.app.android.utils.sha256
import dgca.verifier.app.decoder.toBase64
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.internal.toHexString
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

data class TestConfig(
    val wantedAmountOfHashes: Int,
    val mode: DccRevocationMode
)

data class TestGenerationResult(
    val modeKids: Map<DccRevocationMode, Array<String>>,
    val hashes: List<String>
)

class TestDataGenerationUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<TestGenerationResult, TestConfig>(dispatcher, errorHandler) {

    override suspend fun invoke(params: TestConfig): TestGenerationResult {
        Timber.d("TestDataGenerationUseCase: start generation")
        val amountOfKids = 1
        val wantedAmountOfHashes = params.wantedAmountOfHashes
        val dccMode = params.mode
        // Amount of partitions
        val chunksPerPartition = 16
        val modeKids = generateUniqueKids(amountOfKids, dccMode)

        val amountOfSlicesPerChunk = 10
        var amountOfHashesPerSlice = 0

        if (dccMode == DccRevocationMode.POINT) {
            amountOfHashesPerSlice = wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * amountOfKids)
        }

        if (dccMode == DccRevocationMode.VECTOR) {
            amountOfHashesPerSlice = wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 16 * amountOfKids)
        }

        if (dccMode == DccRevocationMode.COORDINATE) {
            amountOfHashesPerSlice = wantedAmountOfHashes / (chunksPerPartition * amountOfSlicesPerChunk * 256 * amountOfKids)
        }

        var shouldSkipHashesGeneration = false

        modeKids.forEach { (mode, kids) ->
            kids.forEach { kid ->
                val kidMetadata = generateKidMetadata(kid, mode)
                repository.saveKidMetadata(kidMetadata)
            }
        }

        val hashes = mutableListOf<String>()
        val dummyhashes = mutableListOf<String>()
        val partitions = mutableListOf<DccRevocationPartition>()

        val modeHashPrefixes = generateHashPrefixes()
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

                    val partition = DccRevocationPartition(
                        id = "$index",
                        kid = kid,
                        x = x,
                        y = y,
                        ZonedDateTime.now(),
                        chunksString
                    )
                    partitions.add(partition)
                    var counter = 0;
                    val bloomFilter = BloomFilterImpl(amountOfHashesPerSlice, 0.00000000001)
                    chunks.forEach { (cid, slices) ->
                        slices.forEach { (expirationTime, slice) ->
                            val hashStart = hashPrefix + cid

                            if (counter < 2) {

                                bloomFilter.reset(amountOfHashesPerSlice)

                                for (i in 0..amountOfHashesPerSlice) {
                                    val hash = if (shouldSkipHashesGeneration) {
                                        Random.nextInt(Integer.MAX_VALUE).toHexString();
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
                            Timber.d("MYTAG BF bytes amount: ${content.size}, num of elements: $amountOfHashesPerSlice")
                            val sliceLocal = DccRevocationSlice(
                                sid = slice.hash,
                                kid = kid,
                                x = x,
                                y = y,
                                cid = cid,
                                type = SliceType.values().first { it.name == slice.type },
                                version = slice.version,
                                expires = expirationTime,
                                content = content
                            )
                            repository.saveSlice(sliceLocal)
                        }
                    }
                }
            }
        }

        repository.savePartitions(partitions)

        return TestGenerationResult(
            modeKids,
            hashes
        )
    }


    private fun generateUniqueKids(amount: Int, mode: DccRevocationMode): Map<DccRevocationMode, Array<String>> {
        if (amount <= 0) return emptyMap()
        val kids = mutableListOf<String>()
        val ret = mutableMapOf<DccRevocationMode, Array<String>>()
        /*    val pointModeKid = Random.nextBytes(9).toBase64()
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
         return modeKids;*/
        var c = 0;
        while (c < amount) {
            val kid = Random.nextBytes(9).toBase64()
            kids.add(kid)
            c++;
        }
        ret[mode] = kids.toTypedArray();
        return ret
    }

    // Prefixes represent 3 first characters of hashes to be stored in partition.
    private fun generateHashPrefixes(): Map<DccRevocationMode, Array<String>> {
        //if (amount <= 0) return emptyMap()

        val pointModeHashPrefix = ""
        val modeHashPrefixes = mutableMapOf(DccRevocationMode.POINT to arrayOf(pointModeHashPrefix))

        val vectorModePrefixes = mutableSetOf<String>()
        var x = 0
        while (x < hexCharsAmount && vectorModePrefixes.size < 16) {
            val prefix = "${hexChars[x++]}"
            vectorModePrefixes.add(prefix)
        }
        modeHashPrefixes[DccRevocationMode.VECTOR] = vectorModePrefixes.toTypedArray()

        val coordinateModePrefixes = mutableSetOf<String>()
        x = 0
        while (x < hexCharsAmount && coordinateModePrefixes.size < 256) {
            var y = 0
            while (y < hexCharsAmount) {
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
    ): DccRevocationKidMetadata {
        return DccRevocationKidMetadata(
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

    data class Slice(
        val hash: String,
        val type: String = DccSliceType.BF.tag,
        val version: String = "1.0"
    )

    companion object {
        private const val REVOCATION_PATH = "revocation"
        const val REVOCATION_PARTITION_0 = "$REVOCATION_PATH/revocation_partition_0.json"

        private val hexChars =
            arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        private val hexCharsAmount = hexChars.size
        private val amountOfUniqueHexCharsPairs = hexCharsAmount * hexCharsAmount
    }
}