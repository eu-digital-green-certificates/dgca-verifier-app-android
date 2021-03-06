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
 *  Created by mykhailo.nester on 13/01/2022, 12:06
 */

package dcc.app.revocation.data.network

import dcc.app.revocation.data.network.model.RevocationKIDResponse
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import dcc.app.revocation.data.network.model.SliceType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RevocationService {

    @GET
    suspend fun getRevocationLists(
        @Header("If-None-Match") eTag: String,
        @Url url: String
    ): Response<List<RevocationKIDResponse>>

    @GET
    suspend fun getRevocationListPartitions(
        @Header("if-Match") eTag: String,
        @Header("if-modified-since") modifiedSince: String?,
        @Header("X-SLICE-FILTER-TYPE") type: SliceType,
        @Url url: String
    ): Response<List<RevocationPartitionResponse>>

    @POST
    suspend fun getRevocationPartitionChunks(
        @Header("if-Match") eTag: String,
        @Header("X-SLICE-FILTER-TYPE") type: SliceType,
        @Url url: String,
        @Body cidList: List<String>
    ): Response<ResponseBody>

    @GET
    suspend fun getRevocationChunk(
        @Header("If-Match") eTag: String,
        @Header("if-modified-since") modifiedSince: String,
        @Header("X-SLICE-FILTER-TYPE") type: SliceType,
        @Url url: String
    ): Response<ResponseBody>

    @POST
    suspend fun getRevocationChunkSlices(
        @Header("if-Match") eTag: String,
        @Header("if-modified-since") modifiedSince: String,
        @Header("X-SLICE-FILTER-TYPE") type: SliceType,
        @Url url: String,
        @Body sidList: List<String>
    ): Response<ResponseBody>

    @GET
    suspend fun getRevocationChunkSlice(
        @Header("If-Match") eTag: String,
        @Header("if-modified-since") modifiedSince: String,
        @Header("X-SLICE-FILTER-TYPE") type: SliceType,
        @Url url: String
    ): Response<ResponseBody>
}