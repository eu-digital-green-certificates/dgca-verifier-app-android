/*
 *  ---license-start
 *  eu-digital-green-certificates / dcc-revocation-app-android
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
 *  Created by osarapulov on 12/27/21, 10:22 PM
 */

package dgca.verifier.app.android.data.local.dcc.revocation

import dcc.app.revocation.data.source.DccRevocationPartition
import dcc.app.revocation.data.source.local.DccRevocationLocalDataSource

class DccRevocationLocalDataSourceImpl(private val dccRevocationPartitionDao: DccRevocationPartitionDao) :
    DccRevocationLocalDataSource {

    override fun addOrUpdate(dccRevocationPartition: DccRevocationPartition) {
        dccRevocationPartitionDao.insert(dccRevocationPartition.toLocal())
    }

    override fun getBy(
        kid: String,
        firstDccHashByte: Char,
        secondDccHashByte: Char
    ): DccRevocationPartition? {
        return dccRevocationPartitionDao.get(kid, firstDccHashByte, secondDccHashByte)?.fromLocal()
    }

    override fun remove(dccRevocationPartition: DccRevocationPartition) {
        dccRevocationPartitionDao.delete(dccRevocationPartition.toLocal())
    }
}
