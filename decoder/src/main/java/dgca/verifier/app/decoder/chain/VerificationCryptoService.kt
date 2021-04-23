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
 *  Created by Mykhailo Nester on 4/23/21 9:57 AM
 */

package dgca.verifier.app.decoder.chain

import COSE.HeaderKeys
import COSE.KeyKeys
import COSE.OneKey
import com.upokecenter.cbor.CBORObject
import java.math.BigInteger
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

class VerificationCryptoService(private val repository: CertificateRepository) : CryptoService {

    override fun getCborHeaders() = listOf<Pair<HeaderKeys, CBORObject>>()

    override fun getCborSigningKey() = OneKey(CBORObject.NewMap())

    override fun getCborVerificationKey(kid: ByteArray): OneKey {
        val certificate = getCertificate(kid)
        return when (certificate.publicKey) {
            is ECPublicKey -> buildEcKey(certificate.publicKey as ECPublicKey)
            else -> buildRsaKey(certificate.publicKey as RSAPublicKey)
        }
    }

    private fun buildRsaKey(rsaPublicKey: RSAPublicKey): OneKey {
        return OneKey(CBORObject.NewMap().also {
            it[KeyKeys.KeyType.AsCBOR()] = KeyKeys.KeyType_RSA
            it[KeyKeys.RSA_N.AsCBOR()] = stripLeadingZero(rsaPublicKey.modulus)
            it[KeyKeys.RSA_E.AsCBOR()] = stripLeadingZero(rsaPublicKey.publicExponent)
        })
    }

    private fun buildEcKey(publicKey: ECPublicKey): OneKey {
        return OneKey(CBORObject.NewMap().also {
            it[KeyKeys.KeyType.AsCBOR()] = KeyKeys.KeyType_EC2
            it[KeyKeys.EC2_Curve.AsCBOR()] = getEcCurve(publicKey)
            it[KeyKeys.EC2_X.AsCBOR()] = stripLeadingZero(publicKey.w.affineX)
            it[KeyKeys.EC2_Y.AsCBOR()] = stripLeadingZero(publicKey.w.affineY)
        })
    }

    override fun getCertificate(kid: ByteArray) = repository.loadCertificate(kid)

    private fun getEcCurve(publicKey: ECPublicKey) = when (publicKey.params.order.bitLength()) {
        384 -> KeyKeys.EC2_P384
        521 -> KeyKeys.EC2_P521
        else -> KeyKeys.EC2_P256
    }

    // Java's BigInteger adds a leading sign bit
    private fun stripLeadingZero(bigInteger: BigInteger): CBORObject {
        val bytes = bigInteger.toByteArray()
        return when {
            bytes.size % 8 != 0 && bytes[0] == 0x00.toByte() -> CBORObject.FromObject(
                bytes.drop(1).toByteArray()
            )
            else -> CBORObject.FromObject(bytes)
        }
    }
}