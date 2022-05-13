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
 *  Created by mykhailo.nester on 29/03/2022, 13:13
 */

package dgca.verifier.app.android.diia.revocation

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dcc.app.revocation.data.GeneralErrorHandlerImpl
import dcc.app.revocation.data.RevocationPreferences
import dcc.app.revocation.data.RevocationPreferencesImpl
import dcc.app.revocation.data.local.DccRevocationLocalDataSource
import dcc.app.revocation.data.network.RevocationService
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.hexToByteArray
import dcc.app.revocation.domain.model.*
import dcc.app.revocation.domain.usacase.IsDccRevokedUseCase
import dcc.app.revocation.repository.RevocationRepositoryImpl
import dcc.app.revocation.validation.bloom.BloomFilterImpl
import dgca.verifier.app.android.diia.data.local.AppDatabase
import dgca.verifier.app.android.diia.data.local.diia.revocation.DccRevocationDao
import dgca.verifier.app.android.diia.data.local.diia.revocation.DccRevocationLocalDataSourceImpl
import dgca.verifier.app.android.diia.data.local.diia.revocation.mapper.toLocal
import dgca.verifier.app.android.diia.data.local.diia.revocation.model.DccRevocationSliceLocal
import dgca.verifier.app.android.diia.di.BASE_URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.ZonedDateTime

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class IsDccRevokedUseCaseTest {

    private lateinit var dccRevocationDao: DccRevocationDao
    private lateinit var db: AppDatabase

    @ExperimentalCoroutinesApi
    private lateinit var testCoroutineDispatcher: CoroutineDispatcher
    private lateinit var errorHandler: ErrorHandler
    private lateinit var revocationService: RevocationService
    private lateinit var revocationPreferences: RevocationPreferences
    private lateinit var dccRevocationLocalDataSource: DccRevocationLocalDataSource
    private lateinit var revocationRepository: RevocationRepository
    private lateinit var isDccRevokedUseCase: IsDccRevokedUseCase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dccRevocationDao = db.dccRevocationPartitionDao()
        dccRevocationLocalDataSource = DccRevocationLocalDataSourceImpl(dccRevocationDao)
        revocationService = Retrofit.Builder().baseUrl(BASE_URL).build().create(RevocationService::class.java)
        revocationPreferences = RevocationPreferencesImpl(context)
        revocationRepository = RevocationRepositoryImpl(revocationService, revocationPreferences, dccRevocationLocalDataSource)
        errorHandler = GeneralErrorHandlerImpl()
        testCoroutineDispatcher = Dispatchers.Main
        isDccRevokedUseCase = IsDccRevokedUseCase(revocationRepository, testCoroutineDispatcher, errorHandler)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

//    Point mode start

    @Test
    fun uciSearch_hashFilterType_PointMode() = runBlocking {
        val kid = "VpHthrJRPZU="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.POINT)
        val uvciSha256 = "2a503f7cf31159a862e4fdd7c1decc68"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, 42, 80, 63, 124)
        insertSlice(kid, null, null, "2", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_hashFilterType_PointMode() = runBlocking {
        val kid = "VpHthrJRPZU="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.POINT)
        val uvciSha256 = ""
        val coUvciSha256 = "d456131098149f691ae84c915f39503c"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, -44, 86, 19, 16)
        insertSlice(kid, null, null, "d", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_hashFilterType_PointMode() = runBlocking {
        val kid = "VpHthrJRPZU="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.POINT)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "c0cdc101e7979f6a2f65add69174728c"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, -64, -51, -63, 1)
        insertSlice(kid, null, null, "c", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun uciSearch_bloomFilterType_PointMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.POINT)
        val uvciSha256 = "8d2b28f759e723360d33d8dc134639b5"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(uvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, null, null, "8", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_bloomFilterType_PointMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.POINT)
        val uvciSha256 = ""
        val coUvciSha256 = "1d5fb6fa0daf6504e0ffa62a76e1af52"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(coUvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, null, null, "1", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_bloomFilterType_PointMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.POINT)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "25550b0b1a96392737888374121c4505"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(signatureSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, null, null, "2", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

//    Point mode end

//    Vector mode start

    @Test
    fun uciSearch_hashFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.VECTOR)
        val uvciSha256 = "862e4fdd7c1decc682a503f7cf31159a"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, -122, 46, 79, -35)
        insertSlice(kid, '8', null, "6", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_hashFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.VECTOR)
        val uvciSha256 = ""
        val coUvciSha256 = "691ae84c915f39503cd456131098149f"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, 105, 26, -24, 76)
        insertSlice(kid, '6', null, "9", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_hashFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.VECTOR)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "6a2f65add69174728cc0cdc101e7979f"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, 106, 47, 101, -83)
        insertSlice(kid, '6', null, "a", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun uciSearch_bloomFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.VECTOR)
        val uvciSha256 = "0d33d8dc134639b58d2b28f759e72336"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(uvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '0', null, "d", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_bloomFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.VECTOR)
        val uvciSha256 = ""
        val coUvciSha256 = "4e0ffa62a76e1af521d5fb6fa0daf650"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(coUvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '4', null, "e", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_bloomFilterType_VectorMode() = runBlocking {
        val kid = "DfAWD1a3!="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.VECTOR)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "737888374121c450525550b0b1a96392"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(signatureSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '7', null, "3", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

//    Vector mode end

//    Coordinate mode start

    @Test
    fun uciSearch_hashFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.COORDINATE)
        val uvciSha256 = "862e4fdd7c1decc682a503f7cf31159a"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, -122, 46, 79, -35)
        insertSlice(kid, '8', '6', "2", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_hashFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.COORDINATE)
        val uvciSha256 = ""
        val coUvciSha256 = "691ae84c915f39503cd456131098149f"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, 105, 26, -24, 76)
        insertSlice(kid, '6', '9', "1", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_hashFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.COORDINATE)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "6a2f65add69174728cc0cdc101e7979f"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val content = byteArrayOf(0, 1, 43, -116, -68, -52, 0, 0, 0, 1, 4, 106, 47, 101, -83)
        insertSlice(kid, '6', 'a', "2", DccSliceType.VARHASHLIST, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun uciSearch_bloomFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.UCI, DccRevocationMode.COORDINATE)
        val uvciSha256 = "0d33d8dc134639b58d2b28f759e72336"
        val coUvciSha256 = ""
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(uvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '0', 'd', "3", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun coUciSearch_bloomFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.COUNTRYCODEUCI, DccRevocationMode.COORDINATE)
        val uvciSha256 = ""
        val coUvciSha256 = "4e0ffa62a76e1af521d5fb6fa0daf650"
        val signatureSha256 = ""
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(coUvciSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '4', 'e', "0", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

    @Test
    fun signatureSearch_bloomFilterType_coordinateMode() = runBlocking {
        val kid = "jklWer21="
        insertKidMetdata(kid, DccRevocationHashType.SIGNATURE, DccRevocationMode.COORDINATE)
        val uvciSha256 = ""
        val coUvciSha256 = ""
        val signatureSha256 = "737888374121c450525550b0b1a96392"
        val dccRevocationDataHolder = DccRevocationDataHolder(kid, uvciSha256, coUvciSha256, signatureSha256)

        val bloomFilter =
            BloomFilterImpl(1, 1.toByte(), 1)
        bloomFilter.add(signatureSha256.hexToByteArray())
        val content = ByteArrayOutputStream().use {
            bloomFilter.writeTo(it)
            it.toByteArray()
        }
        insertSlice(kid, '7', '3', "7", DccSliceType.BF, content)

        val result = isDccRevokedUseCase.execute(dccRevocationDataHolder)

        assertTrue(result!!)
    }

//    Coordinate mode end

    private fun insertKidMetdata(kid: String, hashType: DccRevocationHashType, mode: DccRevocationMode) {
        // Insert kid metadata
        val dccRevocationKidSignatureMetadata = DccRevocationKidMetadata(
            kid = kid,
            hashType = setOf(hashType),
            mode = mode,
            expires = ZonedDateTime.now().plusYears(1),
            lastUpdated = ZonedDateTime.now()
        )

        dccRevocationDao.insert(dccRevocationKidSignatureMetadata.toLocal())
    }

    private fun insertSlice(kid: String, x: Char?, y: Char?, cid: String, type: DccSliceType, content: ByteArray) {
        val sliceLocal = DccRevocationSliceLocal(
            sid = "testid",
            kid = kid,
            x = x,
            y = y,
            cid = cid,
            type = type,
            version = "1.0",
            expires = ZonedDateTime.now().plusYears(1),
            content = content
        )
        dccRevocationDao.insert(sliceLocal)
    }
}
