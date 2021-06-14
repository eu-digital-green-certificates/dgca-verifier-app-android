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
 *  Created by climent on 6/14/21 1:49 PM
 */

package it.ministerodellasalute.verificaC19.util

import org.junit.Assert
import org.junit.Test

class UtilityTest {

    @Test
    fun `test app version when don't exist an update`() {
        Assert.assertEquals(Utility.versionCompare("1.1", "1.1"), 0)
    }

    @Test
    fun `test app version when exist an update`() {
        Assert.assertEquals(Utility.versionCompare("1.1", "1.2"), -1)
    }

    @Test
    fun `test app version unexpected case`() {
            Assert.assertEquals(Utility.versionCompare("1.2", "1.1"), 1)
    }

}