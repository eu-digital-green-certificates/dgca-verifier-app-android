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
 *  Created by mykhailo.nester on 31/03/2022, 21:22
 */

package dgca.verifier.app.android.vc.data.remote.model

import com.fasterxml.jackson.annotation.JsonProperty

data class JwksKeysResponse(
    @JsonProperty("keys")
    val keys: List<JwkSet>,
)

data class JwkSet(
    @JsonProperty("kty")
    val kty: String,
    @JsonProperty("kid")
    val kid: String,
    @JsonProperty("use")
    val use: String,
    @JsonProperty("alg")
    val alg: String,
    @JsonProperty("x5c")
    val x5c: List<String>?,
    @JsonProperty("crv")
    val crv: String,
    @JsonProperty("x")
    val x: String,
    @JsonProperty("y")
    val y: String,
    @JsonProperty("crlVersion")
    val crlVersion: Long
)

//{
//    "keys": [
//    {
//        "kty": "EC",
//        "kid": "3Kfdg-XwP-7gXyywtUfUADwBumDOPKMQx-iELL11W9s",
//        "use": "sig",
//        "alg": "ES256",
//        "crv": "P-256",
//        "x": "11XvRWy1I2S0EyJlyf_bWfw_TQ5CJJNLw78bHXNxcgw",
//        "y": "eZXwxvO1hvCY0KucrPfKo7yAyMT6Ajc3N7OkAB6VYy8",
//        "crlVersion": 1
//    },
//    {
//        "kty": "EC",
//        "kid": "EBKOr72QQDcTBUuVzAzkfBTGew0ZA16GuWty64nS-sw",
//        "use": "sig",
//        "alg": "ES256",
//        "x5c": [
//        "MIICDDCCAZGgAwIBAgIUVJEUcO5ckx9MA7ZPjlsXYGv+98wwCgYIKoZIzj0EAwMwJzElMCMGA1UEAwwcU01BUlQgSGVhbHRoIENhcmQgRXhhbXBsZSBDQTAeFw0yMTA2MDExNTUwMDlaFw0yMjA2MDExNTUwMDlaMCsxKTAnBgNVBAMMIFNNQVJUIEhlYWx0aCBDYXJkIEV4YW1wbGUgSXNzdWVyMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEPQHApUWm94mflvswQgAnfHlETMwJFqjUVSs7WU6LQy7uaPwg77xXlVmMNtFWwkg0L9GrlqLkIOEVfXxx5GwtZKOBljCBkzAJBgNVHRMEAjAAMAsGA1UdDwQEAwIHgDA5BgNVHREEMjAwhi5odHRwczovL3NwZWMuc21hcnRoZWFsdGguY2FyZHMvZXhhbXBsZXMvaXNzdWVyMB0GA1UdDgQWBBTGqQP/SGBzOjWWcDdk/U7bQFhu+DAfBgNVHSMEGDAWgBQ4uufUcLGAmR55HWQWi+6PN9HJcTAKBggqhkjOPQQDAwNpADBmAjEAlZ9TR2TJnhumSUmtmgsWPpcp3xDYUtcXtxHs2xuHU6HqoaBfWDdUJKO8tWljGSVWAjEApesQltBP8ddWIn1BgBpldJ1pq9zukqfwRjwoCH1SRQXyuhGNfovvQMl/lw8MLIyO",
//        "MIICBzCCAWigAwIBAgIUK9wvDGYJ5S9DKzs/MY+IiTa0CP0wCgYIKoZIzj0EAwQwLDEqMCgGA1UEAwwhU01BUlQgSGVhbHRoIENhcmQgRXhhbXBsZSBSb290IENBMB4XDTIxMDYwMTE1NTAwOVoXDTI2MDUzMTE1NTAwOVowJzElMCMGA1UEAwwcU01BUlQgSGVhbHRoIENhcmQgRXhhbXBsZSBDQTB2MBAGByqGSM49AgEGBSuBBAAiA2IABF2eAAAAGv0/isod1xpgaLX0DASxCDs0+JbCt12CTdQhB7os9m9H8c0nLyaNb8lM9IXkBRZLoLly/ZRaRjU8vq3bt6l5m9Cc6OY+xwmADKvNdNm94dsCC5CiB+JQu6WgWKNQME4wDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUOLrn1HCxgJkeeR1kFovujzfRyXEwHwYDVR0jBBgwFoAUJo6aEvlKNnmPfQaKVkOXIDY87/8wCgYIKoZIzj0EAwQDgYwAMIGIAkIBq9tT76Qzv1wH6nB0/sKPN4xPUScJeDv4+u2Zncv4ySWn5BR3DxYxEdJsVk4Aczw8uBipnYS90XNiogXMmN7JbRQCQgEYLzjOB1BdWIzjBlLF0onqnsAQijr6VX+2tfd94FNgMxHtaU864vgD/b3b0jr/Qf4dUkvF7K9WM1+vbcd0WDP4gQ==",
//        "MIICMjCCAZOgAwIBAgIUadiyU9sUFV6H40ZB5pCyc+gOikgwCgYIKoZIzj0EAwQwLDEqMCgGA1UEAwwhU01BUlQgSGVhbHRoIENhcmQgRXhhbXBsZSBSb290IENBMB4XDTIxMDYwMTE1NTAwOFoXDTMxMDUzMDE1NTAwOFowLDEqMCgGA1UEAwwhU01BUlQgSGVhbHRoIENhcmQgRXhhbXBsZSBSb290IENBMIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB/XU90B0DMB6GKbfNKz6MeEIZ2o6qCX76GGiwhPYZyDLgB4+njRHUA7l7KSrv8THtzXSn8FwDmubAZdbU3lwNRGcAQJVY/9Bq9TY5Utp8ttbVnXcHQ5pumzMgIkkrIzERg+iCZLtjgPYjUMgeLWpqQMG3VBNN6LXN4wM6DiJiZeeBId6jUDBOMAwGA1UdEwQFMAMBAf8wHQYDVR0OBBYEFCaOmhL5SjZ5j30GilZDlyA2PO//MB8GA1UdIwQYMBaAFCaOmhL5SjZ5j30GilZDlyA2PO//MAoGCCqGSM49BAMEA4GMADCBiAJCAe/u808fhGLVpgXyg3h/miSnqxGBx7Gav5Xf3iscdZkF9G5SH1G6UPvIS0tvP/2x9xHh2Vsx82OCZH64uPmKPqmkAkIBcUed8q/dQMgUmsB+jT7A7hKz0rh3CvmhW8b4djD3NesKW3M9qXqpRihd+7KqmTjUxhqUckiPBVLVm5wenaj08Ys="
//        ],
//        "crv": "P-256",
//        "x": "PQHApUWm94mflvswQgAnfHlETMwJFqjUVSs7WU6LQy4",
//        "y": "7mj8IO-8V5VZjDbRVsJINC_Rq5ai5CDhFX18ceRsLWQ"
//    }
//    ]
//}