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

import dcc.app.revocation.data.network.model.SliceType
import dcc.app.revocation.domain.ErrorHandler
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.domain.getDccSignatureSha256
import dcc.app.revocation.domain.model.DccRevocationHashType
import dcc.app.revocation.domain.model.DccRevocationMode
import dcc.app.revocation.domain.model.DccRevokationDataHolder
import dcc.app.revocation.domain.toSha256HexString
import dcc.app.revocation.validation.BloomFilterImpl
import kotlinx.coroutines.CoroutineDispatcher
import java.io.ByteArrayInputStream
import java.io.InputStream
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
            val uciSha256 = params.uci.toByteArray().toSha256HexString()
            containsUvciSha256 = isContainsHash(kid, mode, uciSha256)
        }

        var containsCoUvciSha256 = false
        if (kidMetadata.hashType.contains(DccRevocationHashType.COUNTRYCODEUCI)) {
            val coUvciSha256 = params.coUvci.toByteArray().toSha256HexString()
            containsCoUvciSha256 = isContainsHash(kid, mode, coUvciSha256)
        }

        var containsSignatureSha256 = false
        if (kidMetadata.hashType.contains(DccRevocationHashType.SIGNATURE)) {
            val signatureSha256 = params.cose.getDccSignatureSha256()
            containsSignatureSha256 = isContainsHash(kid, mode, signatureSha256)
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

        val validationData = getValidationData(kid, x, y, cid.toString())
        return contains(hash, validationData)
    }

    private suspend fun getValidationData(kid: String, x: Char?, y: Char?, cid: String): ValidationData? {
        val bloomFilterList = mutableSetOf<ByteArray>()
        val hashList = mutableSetOf<ByteArray>()

        val result = repository.getChunkSlices(kid, x, y, cid)
        result.forEach {
            when (it.type) {
                SliceType.Hash -> hashList.add(it.content)
                SliceType.Bloom -> bloomFilterList.add(it.content)
            }
        }

        return ValidationData(bloomFilterList, hashList)
    }

    private fun contains(dccHash: String, validationData: ValidationData?): Boolean {
        validationData ?: return false

        validationData.bloomFilterList.forEach {
            val inputStream: InputStream = ByteArrayInputStream(it)
            val bloomFilter = BloomFilterImpl(inputStream)
            val contains = bloomFilter.mightContain(dccHash.toByteArray())
            if (contains) {
                return true
            }
        }

        // TODO: hash list check
        return true // validationData.hashList.contains(dccHash)
    }

    internal data class ValidationData(
        val bloomFilterList: Set<ByteArray>,
        val hashList: Set<ByteArray>
    )
}