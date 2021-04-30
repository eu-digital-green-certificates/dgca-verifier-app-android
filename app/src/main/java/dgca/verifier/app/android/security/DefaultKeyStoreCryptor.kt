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
 *  Created by osarapulov on 4/30/21 1:25 AM
 */

package dgca.verifier.app.android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.KeyGenerator
import javax.inject.Inject

class DefaultKeyStoreCryptor @Inject constructor() : KeyStoreCryptor {
    companion object {
        private val TAG = DefaultKeyStoreCryptor::class.java.simpleName

        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "KEY_ALIAS"
    }

    private fun getKeyStore(): KeyStore? {
        try {
            return KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                this.load(null)
            }
        } catch (e: KeyStoreException) {
            Log.i(TAG, null, e)
        } catch (e: CertificateException) {
            Log.i(TAG, null, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.i(TAG, null, e)
        } catch (e: IOException) {
            Log.i(TAG, null, e)
        }
        return null
    }

    override fun encrypt(token: String?): String? {
        val keyStore = getKeyStore()
        return (if (keyStore != null) getSecurityKeyWrapper(keyStore) else null)?.encrypt(token)
    }

    override fun decrypt(token: String?): String? {
        val keyStore = getKeyStore()
        return (if (keyStore != null) getSecurityKeyWrapper(keyStore) else null)?.decrypt(token)
    }

    private fun getSecurityKeyWrapper(keyStore: KeyStore): SecurityKeyWrapper? {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEY_STORE
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setBlockModes(
                        KeyProperties.BLOCK_MODE_GCM
                    )
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                )
                return SecurityKeyWrapper(keyGenerator.generateKey())
            }
        } catch (e: KeyStoreException) {
            Log.i(TAG, null, e)
        } catch (e: NoSuchProviderException) {
            Log.i(TAG, null, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.i(TAG, null, e)
        } catch (e: InvalidAlgorithmParameterException) {
            Log.i(TAG, null, e)
        }
        try {
            val entry = keyStore.getEntry(
                KEY_ALIAS,
                null
            ) as KeyStore.SecretKeyEntry
            return SecurityKeyWrapper(entry.secretKey)
        } catch (e: KeyStoreException) {
            Log.i(TAG, null, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.i(TAG, null, e)
        } catch (e: UnrecoverableEntryException) {
            Log.i(TAG, null, e)
        }
        return null
    }
}