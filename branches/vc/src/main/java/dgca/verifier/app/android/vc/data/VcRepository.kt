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
 *  Created by mykhailo.nester on 23/03/2022, 22:34
 */

package dgca.verifier.app.android.vc.data

import dgca.verifier.app.android.vc.data.remote.model.Jwk
import dgca.verifier.app.android.vc.data.remote.model.SignerCertificate
import dgca.verifier.app.android.vc.data.remote.model.VerificationMethod

interface VcRepository {

    suspend fun loadTrustList(): List<SignerCertificate>

    suspend fun resolveIssuer(url: String): List<Jwk>

    suspend fun resolveIssuerByDid(fullUrl: String): List<VerificationMethod>

    suspend fun saveJWKs(result: List<Jwk>, url: String)

    suspend fun getIssuerJWKsByKid(kid: String): List<Jwk>

    suspend fun removeOutdated()

    suspend fun isIssuerKnown(issuerUrl: String): Boolean
}