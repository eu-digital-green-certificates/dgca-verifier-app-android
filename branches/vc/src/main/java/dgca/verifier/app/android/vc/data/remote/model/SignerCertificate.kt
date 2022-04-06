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

package dgca.verifier.app.android.vc.data.remote.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SignerCertificate(
    val kid: String,
    val timestamp: String,
    val country: String,
    val certificateType: String,
    val thumbprint: String,
    val signature: String,
    val rawData: String
)

//Document Signer Certificate
//{
//    "kid": "qroU+hDDovs=",
//    "timestamp": "2022-02-21T09:54:42.802Z",
//    "country": "DE",
//    "certificateType": "DSC",
//    "thumbprint": "aaba14fa10c3a2fb441a28af0ec1bb4128153b9ddc796b66bfa04b02ea3e103e",
//    "signature": "o53CbAa77LyIMFc5Gz+B2Jc275Gdg/SdLayw7gx0GrTcinR95zfTLr8nNHgJMYlX3rD8Y11zB/Osyt0 ... W+VIrYRGSEmgjGy2EwzvA5nVhsaA+/udnmbyQw9LjAOQ==",
//    "rawData": "MIICyDCCAbCgAwIBAgIGAXR3DZUUMA0GCSqGSIb3DQEBBQUAMBwxCzAJB ... Jpux30QRhsNZwkmEYSbRv+vp5/obgH1mL5ouoV5I="
//  }