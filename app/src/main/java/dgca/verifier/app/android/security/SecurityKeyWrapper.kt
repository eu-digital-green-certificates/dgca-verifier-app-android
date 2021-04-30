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

package dgca.verifier.app.android.security

import android.util.Base64
import android.util.Log
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Wrapper for {@SecretKey} that provide ability to encrypt/decrypt data using it.
 */
class SecurityKeyWrapper(private val secretKey: SecretKey) {
    companion object {
        private val TAG = SecurityKeyWrapper::class.java.simpleName

        private const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
    }

    fun encrypt(token: String?): String? {
        if (token == null) return null
        try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE)
            val encrypted = cipher.doFinal(token.toByteArray())
            return Base64.encodeToString(encrypted, Base64.URL_SAFE)
        } catch (e: GeneralSecurityException) {
            Log.i(TAG, null, e)
        }
        return null
    }

    fun decrypt(encryptedToken: String?): String? {
        if (encryptedToken == null) return null
        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE)
            val decoded = Base64.decode(encryptedToken, Base64.URL_SAFE)
            val original = cipher.doFinal(decoded)
            return String(original)
        } catch (e: GeneralSecurityException) {
            Log.i(TAG, null, e)
        }
        return null
    }

    @Throws(GeneralSecurityException::class)
    private fun getCipher(mode: Int) = Cipher.getInstance(AES_GCM_NO_PADDING).apply {
        init(
            mode,
            secretKey,
            GCMParameterSpec(128, AES_GCM_NO_PADDING.toByteArray(), 0, 12)
        )
    }
}