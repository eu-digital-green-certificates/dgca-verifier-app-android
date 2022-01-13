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

import dcc.app.revocation.data.network.model.RevocationChunkResponse
import dcc.app.revocation.data.network.model.RevocationKIDResponse
import dcc.app.revocation.data.network.model.RevocationPartitionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface RevocationService {

    @Headers("mock:true")
    @GET("/lists")
    suspend fun getRevocationLists(
        @Header("If-None-Match") eTag: String
    ): Response<List<RevocationKIDResponse>>

    //    TODO: If-Match required
    @Headers("mock:true")
    @GET("/{tag}/{kid}/partitions")
    suspend fun getRevocationListPartitions(
        @Path("tag") tag: String,
        @Path("kid") kid: String
    ): Response<List<RevocationPartitionResponse>>

    //    TODO: If-Match required
    @Headers("mock:true")
    @GET("/{kid}/partitions/{id}/chunks/{chunkId}")
    suspend fun getRevocationChunk(
        @Path("kid") kid: String,
        @Path("id") id: String,
        @Path("chunkId") chunkId: String
    ): Response<RevocationChunkResponse>
}