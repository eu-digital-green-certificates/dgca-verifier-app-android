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
 *  Created by osarapulov on 1/11/22, 9:19 AM
 */

package dcc.app.revocation.data

import junit.framework.TestCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZonedDateTime

class DccRevocationChunkTest {
    @Test
    fun shouldWork() {
        val firstHash = "2e7d2c03a9507ae265ecf5b5356885a53393a2029d241394997265a1a25aefc6"
        val secondHash = "3e23e8160039594a33894f6564e1b1348bbd7a0088d42c4acb73eeaed59c009d"
        val thirdHash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb"
        val fourthHash = "18ac3e7343f016890c510e93f935261169d9e3f565436429830faf0934f4f8e4"
        val hashes = listOf(
            firstHash,
            secondHash,
            thirdHash
        ).sorted()

        val chunk = DccRevocationChunk(
            kid = "kid",
            x= null,
            y = null,
            pid = "pid",
            cid = "cid",
            type = DccChunkType.HASH,
            version = "version",
            expiration = ZonedDateTime.now(),
            section = "section",
            content = hashes.joinToString(separator = "")
        )

        assertTrue(chunk.contains(secondHash))
        assertTrue(chunk.contains(firstHash))
        assertTrue(chunk.contains(thirdHash))
        assertFalse(chunk.contains(fourthHash))
    }
}
