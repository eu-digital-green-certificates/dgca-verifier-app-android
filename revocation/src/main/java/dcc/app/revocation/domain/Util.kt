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
 *  Created by mykhailo.nester on 10/01/2022, 18:50
 */

package dcc.app.revocation.domain

import android.util.Base64
import com.upokecenter.cbor.CBORObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


const val ECDSA_256 = -7
const val RSA_PSS_256 = -37

fun ByteArray.getDccSignatureSha256(): String {
    return try {
        val messageObject = CBORObject.DecodeFromBytes(this)
        val protectedHeader = messageObject[0].GetByteString()
        val unprotectedHeader = messageObject[1]
        val coseSignature = messageObject.get(3).GetByteString()

        return when (getAlgoFromHeader(protectedHeader, unprotectedHeader)) {
            ECDSA_256 -> {
                val len = coseSignature.size / 2
                val r = Arrays.copyOfRange(coseSignature, 0, len)
                r.toSha256HexString()
            }
            RSA_PSS_256 -> coseSignature.toSha256HexString()
            else -> ""
        }
    } catch (ex: Exception) {
        ""
    }
}

private fun getAlgoFromHeader(protectedHeader: ByteArray, unprotectedHeader: CBORObject): Int {
    return if (protectedHeader.isNotEmpty()) {
        try {
            val algo = CBORObject.DecodeFromBytes(protectedHeader).get(1)
            algo?.AsInt32Value() ?: unprotectedHeader.get(1).AsInt32Value()
        } catch (ex: Exception) {
            unprotectedHeader.get(1).AsInt32Value()
        }
    } else {
        unprotectedHeader.get(1).AsInt32Value()
    }
}

fun ByteArray.toSha256HexString(): String = sha256Short()?.joinToString("") { "%02x".format(it) } ?: ""

//  Cut off the first 128 bit, gateway has a definition that just the first 128 bits are shared
fun ByteArray.sha256Short(): ByteArray? {
    return try {
        MessageDigest.getInstance("SHA-256").digest(this).copyOfRange(0, 16)
    } catch (e: NoSuchAlgorithmException) {
        null
    }
}

fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

fun String.hexToByteArray(): ByteArray = chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()

fun String.toBase64Url(): String =
    Base64.encodeToString(Base64.decode(this, Base64.DEFAULT), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)