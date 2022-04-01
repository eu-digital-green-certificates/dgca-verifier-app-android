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
import com.nimbusds.jose.jwk.ECKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.android.vc.data.JwsTokenParser
import dgca.verifier.app.android.vc.data.remote.VcApiService
import dgca.verifier.app.android.vc.data.remote.model.Jwk
import dgca.verifier.app.android.vc.fromBase64Url
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.security.Signature
import javax.inject.Inject

@HiltViewModel
class VcViewModel @Inject constructor(
    private val jwsTokenParser: JwsTokenParser,
    private val vcApiService: VcApiService
) : ViewModel() {

    private val _event = MutableLiveData<Event<ViewEvent>>()
    val event: LiveData<Event<ViewEvent>> = _event

    fun validate(jwt: String) {
        viewModelScope.launch {
            val jwsObject = jwsTokenParser.parse(jwt) ?: return@launch
            val header = jwsObject.header
            val payload = jwsObject.payload
            val signature = jwsObject.signature
            val issuer = payload.iss
            val kid = header.kid

            if (kid.isEmpty()) {
                _event.value = Event(ViewEvent.OnError(ErrorType.KID_NOT_INCLUDED))
                return@launch
            }

            if (issuer.isEmpty()) {
                _event.value = Event(ViewEvent.OnError(ErrorType.ISSUER_NOT_INCLUDED))
                return@launch
            }

            val publicKeys = if (URLUtil.isValidUrl(issuer)) {
                resolveIssuer(kid, "$issuer/.well-known/jwks.json")
            } else {
                resolveDid(kid, issuer)
            }

            val isSignatureValid = false
            publicKeys.forEach {
                verifyJws(it, jwt)
            }

        }
    }

    private suspend fun resolveIssuer(kid: String, url: String): List<Jwk> {
        return try {
            vcApiService.resolveIssuer(url).body()?.keys?.filter { it.kid == kid } ?: emptyList()

        } catch (ex: IOException) {
            Timber.e(ex, "Failed to fetch jwk")
            emptyList()
        }
    }

    private suspend fun resolveDid(kid: String, issuer: String): List<Jwk> {
//        TODO: handle DID document

        return emptyList()
    }

    private fun verifyJws(jwk: Jwk, tokenString: String) {
        val valid = verify(tokenString, jwk)
        Timber.d("isValid: $valid")
    }

    fun verify(jwt: String, jwk: Jwk): Boolean {
        val publicKey = ECKey.parse(ObjectMapper().writeValueAsString(jwk)).toECPublicKey()

        val splitJwt: List<String> = jwt.split('.')
        val headerStr = splitJwt[0]
        val payloadStr = splitJwt[1]
        val signatureStr = splitJwt[2]

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(headerStr.toByteArray() + '.'.code.toByte() + payloadStr.toByteArray())
        val result = signature.verify(signatureStr.fromBase64Url())
        return result

//        val rsa = try {
//            val kf = KeyFactory.getInstance("RSA")
//
//            val modulus = BigInteger(1, jwk.x.fromBase64Url())
//            val exponent = BigInteger(1, jwk.y.fromBase64Url())
//            kf.generatePublic(RSAPublicKeySpec(modulus, exponent))
//        } catch (e: InvalidKeySpecException) {
//            e.printStackTrace()
//            null
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//            null
//        }
//
//        return if (rsa == null) {
//            false
//        } else {
//            val parts = jwt.split('.')
//
//            if (parts.size == 3) {
//                val header = parts[0].fromBase64Url()
//                val payload = parts[1].fromBase64Url()
//                val tokenSignature = parts[2].fromBase64Url()
//
//                val rsaSignature = Signature.getInstance("SHA256withRSA")
//                rsaSignature.initVerify(rsa)
//                rsaSignature.update(header)
//                rsaSignature.update('.'.code.toByte())
//                rsaSignature.update(payload)
//                rsaSignature.verify(tokenSignature)
//            } else {
//                false
//            }
//        }
    }

    sealed class ViewEvent {
        data class OnError(val type: ErrorType) : ViewEvent()
    }

    enum class ErrorType {
        KID_NOT_INCLUDED,
        ISSUER_NOT_INCLUDED,
        VERIFIED,
        INVALID_SIGNATURE
    }
}