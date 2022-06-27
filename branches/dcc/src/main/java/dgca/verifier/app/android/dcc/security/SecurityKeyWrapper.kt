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
 *  Created by osarapulov on 4/30/21 1:33 AM
 */

package dgca.verifier.app.android.dcc.security

import android.util.Base64
import timber.log.Timber
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Wrapper for {@SecretKey} that provide ability to encrypt/decrypt data using it.
 */
class SecurityKeyWrapper(private val secretKey: SecretKey) {

    private val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)

    fun encrypt(token: String?): String? {
        if (token == null) return null

        return try {
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                GCMParameterSpec(128, iv, 0, 12)
            )

            val ivString = Base64.encodeToString(iv, Base64.URL_SAFE)
            val result = StringBuilder(ivString)
            result.append(IV_SEPARATOR)

            val bytes = cipher.doFinal(token.toByteArray())
            result.append(Base64.encodeToString(bytes, Base64.URL_SAFE))
            result.toString()
        } catch (ex: Exception) {
            Timber.w(ex)
            null
        }
    }

    fun decrypt(encryptedToken: String?): String? {
        if (encryptedToken == null) return null

        return try {
            val split = encryptedToken.split(IV_SEPARATOR.toRegex())
            if (split.size != 2) throw IllegalArgumentException("Passed data is incorrect. There was no IV specified with it.")

            val ivString = split[0]
            val encodedString = split[1]
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(128, Base64.decode(ivString, Base64.URL_SAFE), 0, 12)
            )

            val encryptedData = Base64.decode(encodedString, Base64.URL_SAFE)
            val decodedData = cipher.doFinal(encryptedData)
            String(decodedData)
        } catch (ex: Exception) {
            Timber.w(ex)
            null
        }
    }

    companion object {
        private const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"
    }
}
