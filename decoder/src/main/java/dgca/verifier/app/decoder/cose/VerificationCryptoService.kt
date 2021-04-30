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
 *  Created by mykhailo.nester on 4/24/21 3:06 PM
 */

package dgca.verifier.app.decoder.cose

import com.upokecenter.cbor.CBORObject
import dgca.verifier.app.decoder.model.VerificationResult
import java.security.Signature
import java.security.cert.Certificate

class VerificationCryptoService : CryptoService {

    override fun validate(cose: ByteArray, certificate: Certificate, verificationResult: VerificationResult) {
        val verificationKey = certificate.publicKey
        verificationResult.coseVerified = try {
            val messageObject = CBORObject.DecodeFromBytes(cose)
            val coseSignature = messageObject.get(3).GetByteString()
            val protected = messageObject[0].GetByteString()
            val content = messageObject[2].GetByteString()

            val dataToBeVerified = getValidationData(protected, content)

            val signature = Signature.getInstance("SHA256withRSA/PSS")
            signature.initVerify(verificationKey)
            signature.update(dataToBeVerified)
            signature.verify(coseSignature)

        } catch (ex: Exception) {
            false
        }
    }

    private fun getValidationData(protected: ByteArray, content: ByteArray): ByteArray {
        return CBORObject.NewArray().apply {
            Add("Signature1")
            Add(protected)
            Add(ByteArray(0))
            Add(content)
        }.EncodeToBytes()
    }
}