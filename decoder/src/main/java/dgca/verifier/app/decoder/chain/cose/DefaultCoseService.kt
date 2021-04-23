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
 *  Created by Mykhailo Nester on 4/23/21 9:51 AM
 */

package dgca.verifier.app.decoder.chain.cose

import COSE.HeaderKeys
import COSE.MessageTag
import COSE.Sign1Message
import dgca.verifier.app.decoder.chain.CryptoService
import dgca.verifier.app.decoder.chain.model.VerificationResult

class DefaultCoseService(private val cryptoService: CryptoService) : CoseService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): ByteArray {
        verificationResult.coseVerified = false
        return try {
            (Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message).also {
                getKid(it)?.let { kid ->
                    try {
                        val verificationKey = cryptoService.getCborVerificationKey(kid)
                        verificationResult.coseVerified = it.validate(verificationKey)
                    } catch (e: Throwable) {
                        it.GetContent()
                    }
                }
            }.GetContent()
        } catch (e: Throwable) {
            input
        }
    }

    private fun getKid(it: Sign1Message): ByteArray? {
        val key = HeaderKeys.KID.AsCBOR()
        if (it.protectedAttributes.ContainsKey(key)) {
            return it.protectedAttributes.get(key).GetByteString()
        } else if (it.unprotectedAttributes.ContainsKey(key)) {
            return it.unprotectedAttributes.get(key).GetByteString()
        }
        return null
    }
}