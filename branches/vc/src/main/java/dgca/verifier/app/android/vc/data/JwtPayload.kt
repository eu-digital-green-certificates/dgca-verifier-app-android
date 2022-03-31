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
 *  Created by mykhailo.nester on 25/03/2022, 23:22
 */

package dgca.verifier.app.android.vc.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

@JsonIgnoreProperties(ignoreUnknown = true)
class JwtPayload {
    @JsonProperty("iss")
    val iss: String = ""
    @JsonProperty("nbf")
    val nbf: Long = -1

    //    @JsonProperty("sub")
//    val sub: String,
//    @JsonProperty("jti")
//    val jti: String,
//    @JsonProperty("iat")
//    val iat: Long,
//    @JsonProperty("exp")
//    val exp: Long,
//    @JsonProperty("nonce")
//    val nonce: String,
//    @JsonProperty("vc")
    var verifiableCredentials: String? = null

    @JsonProperty("vc")
    fun unpackNested(vc: Map<String, Object>) {
        verifiableCredentials = ObjectMapper().writeValueAsString(vc)
    }
}

// TODO: check if needed
//data class Credentials(
//    @JsonProperty("type")
//    val type: List<String>,
//    @JsonProperty("credentialSubject")
//    val credentialSubject: String,
//    @JsonProperty("rid")
//    val rid: String,
//)

//{
//   "iss":"https://spec.smarthealth.cards/examples/issuer",
//   "nbf":1646083019.674,
//   "vc":{
//      "type":[
//         "https://smarthealth.cards#health-card",
//         "https://smarthealth.cards#immunization",
//         "https://smarthealth.cards#covid19"
//      ],
//      "credentialSubject":{
//         "fhirVersion":"4.0.1",
//         "fhirBundle":{
//            "resourceType":"Bundle",
//            "type":"collection",
//            "entry":[
//               {
//                  "fullUrl":"resource:0",
//                  "resource":{
//                     "resourceType":"Patient",
//                     "name":[
//                        {
//                           "family":"Revoked",
//                           "given":[
//                              "Johnny"
//                           ]
//                        }
//                     ],
//                     "birthDate":"1960-04-22"
//                  }
//               },
//               {
//                  "fullUrl":"resource:1",
//                  "resource":{
//                     "resourceType":"Immunization",
//                     "status":"completed",
//                     "vaccineCode":{
//                        "coding":[
//                           {
//                              "system":"http://hl7.org/fhir/sid/cvx",
//                              "code":"207"
//                           }
//                        ]
//                     },
//                     "patient":{
//                        "reference":"resource:0"
//                     },
//                     "occurrenceDateTime":"2021-03-01",
//                     "performer":[
//                        {
//                           "actor":{
//                              "display":"ABC General Hospital"
//                           }
//                        }
//                     ],
//                     "lotNumber":"0000003"
//                  }
//               },
//               {
//                  "fullUrl":"resource:2",
//                  "resource":{
//                     "resourceType":"Immunization",
//                     "status":"completed",
//                     "vaccineCode":{
//                        "coding":[
//                           {
//                              "system":"http://hl7.org/fhir/sid/cvx",
//                              "code":"207"
//                           }
//                        ]
//                     },
//                     "patient":{
//                        "reference":"resource:0"
//                     },
//                     "occurrenceDateTime":"2021-03-29",
//                     "performer":[
//                        {
//                           "actor":{
//                              "display":"ABC General Hospital"
//                           }
//                        }
//                     ],
//                     "lotNumber":"0000009"
//                  }
//               }
//            ]
//         }
//      },
//      "rid":"vwAjHdarZuc"
//   },
//   "exp":1677619019.674
//}