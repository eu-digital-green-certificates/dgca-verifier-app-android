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
import dgca.verifier.app.decoder.chain.model.CoseData
import dgca.verifier.app.decoder.chain.model.VerificationResult

class DefaultCoseService : CoseService {

    override fun decode(input: ByteArray, verificationResult: VerificationResult): CoseData? {
        verificationResult.coseVerified = false
        return try {
            val sign1Message = (Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message)
            val kid = getKid(sign1Message)
            CoseData(sign1Message, sign1Message.GetContent(), kid)

        } catch (e: Throwable) {
            null
        }
    }

    private fun getKid(sign1Message: Sign1Message): ByteArray? {
        val key = HeaderKeys.KID.AsCBOR()
        if (sign1Message.protectedAttributes.ContainsKey(key)) {
            return sign1Message.protectedAttributes.get(key).GetByteString()
        } else if (sign1Message.unprotectedAttributes.ContainsKey(key)) {
            return sign1Message.unprotectedAttributes.get(key).GetByteString()
        }
        return null
    }
}

