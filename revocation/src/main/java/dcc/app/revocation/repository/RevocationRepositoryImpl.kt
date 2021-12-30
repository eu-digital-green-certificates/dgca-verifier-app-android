/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
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
 *  Created by mykhailo.nester on 24/12/2021, 15:29
 */

package dcc.app.revocation.repository

import dcc.app.revocation.data.containsServerError
import dcc.app.revocation.domain.RevocationRepository
import dcc.app.revocation.network.RevocationService
import dcc.app.revocation.network.model.RevocationKIDData
import retrofit2.HttpException
import javax.inject.Inject

class RevocationRepositoryImpl @Inject constructor(
    private val revocationService: RevocationService
) : RevocationRepository {

    @Throws(Exception::class)
    override suspend fun getRevocationLists(): List<RevocationKIDData> {
        // TODO: add eTag in preferences
        val eTag = ""
        val response = revocationService.getRevocationLists(eTag)

        if (response.containsServerError()) {
            throw HttpException(response)
        }
        return response.body() ?: emptyList()
    }

    @Throws(Exception::class)
    override suspend fun getRevocationListPartitions(kid: String): ByteArray {
        val response = revocationService.getRevocationListPartitions(kid)

        if (response.containsServerError()) {
            throw HttpException(response)
        }
        return response.body()?.bytes() ?: byteArrayOf()
    }

    @Throws(Exception::class)
    override suspend fun getRevocationListChunks(kid: String, id: String): List<String> {
//        val response = revocationService.getRevocationListChunks(kid, id) TODO: update api
//
//        if (response.containsServerError()) {
//            throw HttpException(response)
//        }
//        return response.body()?.toRevocationList() ?: ...
        return listOf("list chunk")
    }

    @Throws(Exception::class)
    override suspend fun getRevocationChunk(kid: String, id: String, chunkId: String): List<String> {
//        val response = revocationService.getRevocationChunk(kid, id, chunkId) TODO: update api
//
//        if (response.containsServerError()) {
//            throw HttpException(response)
//        }
//        return response.body()?.toRevocationList() ?: ...
        return listOf("chunk")
    }
}