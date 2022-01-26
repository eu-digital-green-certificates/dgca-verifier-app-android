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
 *  Created by mykhailo.nester on 20/01/2022, 18:01
 */

package dcc.app.revocation.domain.usacase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dcc.app.revocation.data.network.model.Slice
import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.hexToByteArray
import dcc.app.revocation.domain.model.DccRevocationHashType
import dcc.app.revocation.domain.model.DccRevocationMode
import dcc.app.revocation.domain.model.DccRevokationDataHolder
import dcc.app.revocation.validation.BloomFilterImpl
import kotlinx.coroutines.CoroutineDispatcher
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.reflect.Type
import javax.inject.Inject

class IsDccRevokedUseCase @Inject constructor(
    private val repository: RevocationRepository,
    dispatcher: CoroutineDispatcher,
    errorHandler: ErrorHandler,
) : BaseUseCase<Boolean, DccRevokationDataHolder>(dispatcher, errorHandler) {

    override suspend fun invoke(params: DccRevokationDataHolder): Boolean {
        val kid = params.kid
        val kidMetadata = repository.getMetadataByKid(kid)
        kidMetadata ?: return false

        val mode = kidMetadata.mode
        var containsUvciSha256 = false
        if (kidMetadata.hashType.contains(DccRevocationHashType.UCI)) {
            containsUvciSha256 = isContainsHash(kid, mode, params.uvciSha256)
        }

        var containsCoUvciSha256 = false
        if (kidMetadata.hashType.contains(DccRevocationHashType.COUNTRYCODEUCI)) {
            containsCoUvciSha256 = isContainsHash(kid, mode, params.coUvciSha256)
        }

        var containsSignatureSha256 = false
        if (kidMetadata.hashType.contains(DccRevocationHashType.COUNTRYCODEUCI)) {
            containsSignatureSha256 = isContainsHash(kid, mode, params.signatureSha256)
        }

        return containsUvciSha256 || containsCoUvciSha256 || containsSignatureSha256
    }

    private suspend fun isContainsHash(kid: String, mode: DccRevocationMode, hash: String?): Boolean {
        hash ?: return false

        var x: Char? = null
        var y: Char? = null
        val cid: Char

        when (mode) {
            DccRevocationMode.POINT -> {
                cid = hash[0]
            }
            DccRevocationMode.VECTOR -> {
                cid = hash[1]
                x = hash[0]
            }
            DccRevocationMode.COORDINATE -> {
                cid = hash[2]
                x = hash[0]
                y = hash[1]
            }
            DccRevocationMode.UNKNOWN -> return false
        }

        val validationData = getValidationData(kid, x, y, cid)
        return contains(hash, validationData)
    }

    private suspend fun getValidationData(kid: String, x: Char?, y: Char?, cid: Char): ValidationData? {
        val partition = repository.getRevocationPartition(kid, x, y)
        val chunks = partition?.chunks ?: return null

        val type: Type = object : TypeToken<Map<String, Map<String, Slice>>>() {}.type
        val localChunks = Gson().fromJson<Map<String, Map<String, Slice>>>(chunks, type)

        val bloomFilterList = mutableListOf<String>()
        val hashList = mutableListOf<String>()

        val slices = localChunks[cid.toString()]
        slices?.values?.map { it.hash }?.let { sliceIds ->
            repository.getChunkSlices(sliceIds, kid, x, y, cid)?.let {
                when (it.type) {
                    SliceType.Hash -> hashList.add(it.content)
                    SliceType.Bloom -> bloomFilterList.add(it.content)
                }
            }
        }

        return ValidationData(bloomFilterList, hashList)
    }

    private fun contains(dccHash: String, validationData: ValidationData?): Boolean {
        validationData ?: return false

        validationData.bloomFilterList.forEach {
            val inputStream: InputStream = ByteArrayInputStream(it.hexToByteArray())
            val bloomFilter = BloomFilterImpl(inputStream)
            val contains = bloomFilter.mightContain(dccHash.toByteArray())
            if (contains) {
                return true
            }
        }


        return validationData.hashList.contains(dccHash)

//        validationData.bloomFilterList.
//  TODO: add validation
//        return false
    }

    internal data class ValidationData(
        val bloomFilterList: List<String>,
        val hashList: List<String>
    )
}