/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 7/26/21 11:56 AM
 */

package dgca.verifier.app.android.vc.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dgca.verifier.app.android.vc.data.remote.model.IssuerType

@Entity(tableName = "cert_issuer")
data class CertificateIssuerLocal(
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "type")
    val type: IssuerType,
    @ColumnInfo(name = "country")
    val country: String,
    @ColumnInfo(name = "thumbprint")
    val thumbprint: String?,
    @ColumnInfo(name = "ssl_public_key")
    val sslPublicKey: String,
    @ColumnInfo(name = "key_storage_type")
    val keyStorageType: String,
    @ColumnInfo(name = "signature")
    val signature: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "jwk_list")
    val jwkList: String
)