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
 *  Created by mykhailo.nester on 24/12/2021, 15:23
 */

package dcc.app.revocation.network

import dcc.app.revocation.network.model.RevocationKIDData
import okhttp3.ResponseBody
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
    ): Response<List<RevocationKIDData>>

    @Headers("mock:true")
    @GET("/{kid}/partitions")
    suspend fun getRevocationListPartitions(
        @Path("kid") kid: String
    ): Response<ResponseBody>

    @GET("/{kid}/partitions/{id}/chunks")
    suspend fun getRevocationListChunks(
        @Path("kid") kid: String,
        @Path("id") id: String
    ): ResponseBody // TODO: update response model

    @GET("/{kid}/partitions/{id}/chunks/{chunkId}")
    suspend fun getRevocationChunk(
        @Path("kid") kid: String,
        @Path("id") id: String,
        @Path("chunkId") chunkId: String
    ): ResponseBody // TODO: update response model
}