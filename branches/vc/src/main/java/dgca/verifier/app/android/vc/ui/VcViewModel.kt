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
 *  Created by mykhailo.nester on 25/03/2022, 23:02
 */

package dgca.verifier.app.android.vc.ui

import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.json.JsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import com.nimbusds.jose.jwk.ECKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.vc.data.VcRepository
import dgca.verifier.app.android.vc.data.remote.model.IssuerType
import dgca.verifier.app.android.vc.data.remote.model.Jwk
import dgca.verifier.app.android.vc.inflate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URI
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToLong

const val KEY_ZIP = "zip"
const val ISSUER = "iss"
const val TIME_NOT_BEFORE = "nbf"
const val TIME_EXPIRES = "exp"
const val DEFLATE = "DEF"
const val DID = "did:web:"
const val TYPE_HTTP_SUFFIX = "/.well-known/jwks.json"

@HiltViewModel
class VcViewModel @Inject constructor(
    private val vcRepository: VcRepository
) : ViewModel() {

    private val _event = MutableLiveData<Event<ViewEvent>>()
    val event: LiveData<Event<ViewEvent>> = _event

    private var jwsObject: JWSObject? = null
    private var issuerHolder: IssuerHolder? = null
    private var payloadUnzipString = ""
    private var kid = ""

    init {
        Configuration.setDefaults(object : Configuration.Defaults {
            private val jsonProvider: JsonProvider = GsonJsonProvider()
            private val mappingProvider: MappingProvider = GsonMappingProvider()

            override fun jsonProvider(): JsonProvider = jsonProvider
            override fun mappingProvider(): MappingProvider = mappingProvider
            override fun options(): Set<Option> = EnumSet.noneOf(Option::class.java)
        })
    }

    fun validate(input: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                decodeJwsInput(input)
            }
        }
    }

    private suspend fun decodeJwsInput(input: String) {
        jwsObject = decodeJws(input)

        val jws = jwsObject
        if (jws == null) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.JWS_STRUCTURE_NOT_VALID)))
            return
        }

        val zip = jws.header.customParams[KEY_ZIP]
        var payloadObject = mapOf<String, Any>()
        if (zip == DEFLATE) {
            val payloadUnzip = inflate(jws.payload.toBytes())
            payloadUnzipString = payloadUnzip.toString(Charsets.UTF_8)
            payloadObject = Payload(payloadUnzip).toJSONObject()
        } else {
            jws.payload.toJSONObject()
        }

        val issuer = payloadObject[ISSUER] as String
        val notBefore = (payloadObject[TIME_NOT_BEFORE] as Double).roundToLong()
        val expires = (payloadObject[TIME_EXPIRES] as? Double)?.roundToLong()

        kid = jws.header.keyID

        if (kid.isEmpty()) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.KID_NOT_INCLUDED)))
            return
        }

        if (issuer.isEmpty()) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.ISSUER_NOT_INCLUDED)))
            return
        }

        val now = System.currentTimeMillis() / 1000
        if (now < notBefore) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.TIME_BEFORE_NBF)))
            return
        }

        if (expires != null && now > expires) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.VC_EXPIRED)))
            return
        }

        val result = vcRepository.getIssuerJWKsByKid(kid)
        if (result.isEmpty()) {
            issuerHolder = when {
                URLUtil.isValidUrl(issuer) -> IssuerHolder("$issuer$TYPE_HTTP_SUFFIX", IssuerType.HTTP)
                issuer.contains(DID) -> {
                    val didUrl = issuer.drop(DID.length).replace(":", "/")
                    val url = if (didUrl.contains("/")) {
                        "https://${didUrl}/did.json"
                    } else {
                        "https://${didUrl}/.well-known/did.json"
                    }
                    IssuerHolder(url, IssuerType.DID)
                }
                else -> {
                    _event.postValue(Event(ViewEvent.OnError(ErrorType.ISSUER_NOT_RECOGNIZED)))
                    return
                }
            }

            _event.postValue(Event(ViewEvent.OnIssuerNotTrusted(URI(issuerHolder!!.issuerUrl).host)))
            return
        }

        validateJws(result)
    }

    fun issuerApproved() {
        viewModelScope.launch {
            val holder = issuerHolder ?: return@launch

            withContext(Dispatchers.IO) {
                val result = when (holder.type) {
                    IssuerType.HTTP -> resolveIssuer(kid, holder.issuerUrl)
                    IssuerType.DID -> resolveDid(kid, holder.issuerUrl)
                    else -> emptyList()
                }
                validateJws(result)
            }
        }
    }

    private fun validateJws(list: List<Jwk>) {
        var isSignatureValid = false
        list.forEach {
            if (verifyJws(it, jwsObject)) {
                isSignatureValid = true
            }
        }

        if (!isSignatureValid) {
            _event.postValue(Event(ViewEvent.OnError(ErrorType.INVALID_SIGNATURE)))
            return
        } else {
            val jsonContext: DocumentContext = JsonPath.parse(payloadUnzipString)
            val typeRef: TypeRef<List<List<SubjectName>>> = object : TypeRef<List<List<SubjectName>>>() {}
            val subjectName = jsonContext.read("\$.vc.credentialSubject..name", typeRef).first().first()

//                TODO: parsing of dynamic data
            val type: Type = object : TypeToken<Map<String?, Any?>?>() {}.type
            val data: Map<String, Any> = Gson().fromJson(payloadUnzipString, type)
            val newMap: MutableMap<String, String> = HashMap()
            process("vc", data["vc"]!!, newMap)
            val result = GsonBuilder().setPrettyPrinting().create().toJson(newMap)
            Timber.d("Test: $result")

            _event.postValue(Event(ViewEvent.OnVerified(subjectName, result.replace("[{\",}]".toRegex(), "").trim())))
        }
    }

    private fun process(key: String, value: Any, newMap: MutableMap<String, String>) {
        when (value) {
            is String -> newMap[key] = value
            is Map<*, *> -> {
                val map = value as Map<String, Any>
                for ((key1, value1) in map) {
                    process(key1, value1, newMap)
                }
            }
            is List<*> -> {
                val list = value as List<Any>
                for (obj in list) {
                    process(key, obj, newMap)
                }
            }
        }
    }

    private fun decodeJws(jws: String): JWSObject? =
        try {
            JWSObject.parse(jws)
        } catch (ex: ParseException) {
            Timber.e(ex, "JWS parsing exception")
            null
        }

    private suspend fun resolveIssuer(kid: String, url: String): List<Jwk> =
        vcRepository.resolveIssuer(url).filter { it.kid == kid }

    private suspend fun resolveDid(kid: String, issuer: String): List<Jwk> =
        vcRepository.resolveIssuerByDid(issuer)
            .filter { it.publicKeyJwk.kid == kid }
            .map { it.publicKeyJwk }

    private fun verifyJws(jwk: Jwk, jws: JWSObject?): Boolean {
        jws ?: return false

        return try {
            val publicKey = ECKey.parse(ObjectMapper().writeValueAsString(jwk)).toECPublicKey()
            val verifier = DefaultJWSVerifierFactory().createJWSVerifier(jws.header, publicKey)

            val isVerified = jws.verify(verifier)
            Timber.d("JWS signature isValid: $isVerified")
            isVerified

        } catch (ex: Exception) {
            Timber.e(ex, "JWS signing verification exception")
            false
        }
    }

    sealed class ViewEvent {
        data class OnIssuerNotTrusted(val issuerDomain: String) : ViewEvent()
        data class OnError(val type: ErrorType) : ViewEvent()
        data class OnVerified(val subjectName: SubjectName, val payloadInfo: String) : ViewEvent() // TODO: add payload
    }

    enum class ErrorType {
        JWS_STRUCTURE_NOT_VALID,
        KID_NOT_INCLUDED,
        ISSUER_NOT_RECOGNIZED,
        ISSUER_NOT_INCLUDED,
        TIME_BEFORE_NBF,
        VC_EXPIRED,
        INVALID_SIGNATURE
    }

    data class SubjectName(
        val family: String,
        val given: List<String>
    )

    data class IssuerHolder(
        val issuerUrl: String,
        val type: IssuerType
    )
}