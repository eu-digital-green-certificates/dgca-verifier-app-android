/*
 *  ---license-start
 *  eu-digital-covid-certificates / dcc-verifier-app-android
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
 *  Created by mykhailo.nester on 23/03/2022, 21:37
 */

package dgca.verifier.app.android.vc.data.remote

import dgca.verifier.app.android.vc.data.remote.model.JwksKeysResponse
import dgca.verifier.app.android.vc.data.remote.model.SignerCertificate
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface VcApiService {

    @GET("/trustList")
    suspend fun fetchTrustList(): List<SignerCertificate>

    @GET
    suspend fun resolveIssuer(@Url url: String): Response<JwksKeysResponse>
}