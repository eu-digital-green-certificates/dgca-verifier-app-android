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
 *  Created by osarapulov on 12/27/21, 9:58 PM
 */

package dcc.app.revocation.data.source

import dcc.app.revocation.data.DccRevocationEntry
import dcc.app.revocation.data.DccRevocationKidMetadata
import dcc.app.revocation.data.DccRevocationPartition
import dcc.app.revocation.data.source.local.DccRevocationLocalDataSource
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class DccRevocationRepositoryImpl @Inject constructor(
    private val dccRevocationLocalDataSource: DccRevocationLocalDataSource
) : DccRevocationRepository {

    companion object {
        private const val LONG_STRING_LENGTH = 19
        private const val SHA_256_STRING_LENGTH = 64
    }

    override fun addOrUpdate(dccRevocationKidMetadata: DccRevocationKidMetadata) {
        dccRevocationLocalDataSource.addOrUpdate(dccRevocationKidMetadata)
    }

    override fun removeDccRevocationKidMetadataBy(kid: String) {
        dccRevocationLocalDataSource.removeDccRevocationKidMetadataBy(kid)
    }

    override fun add(
        kid: String,
        dccRevocationHash: String,
        dccRevocationExpirationDate: ZonedDateTime
    ) {
        val currentRevocationPartition: DccRevocationPartition? =
            dccRevocationLocalDataSource.getBy(kid, dccRevocationHash[0], dccRevocationHash[1])

        val dccRevocationExpirationTimestamp = String.format(
            "%0${LONG_STRING_LENGTH}d",
            dccRevocationExpirationDate.toInstant().toEpochMilli()
        )

        val dccHashIndex: Int =
            currentRevocationPartition?.revocationDataBlob?.indexOf(dccRevocationHash) ?: -1
        val newHashSubString = if (dccHashIndex >= 0) {
            currentRevocationPartition?.revocationDataBlob!!.replaceRange(
                dccHashIndex + SHA_256_STRING_LENGTH,
                dccHashIndex + SHA_256_STRING_LENGTH + LONG_STRING_LENGTH,
                dccRevocationExpirationTimestamp
            )
        } else {
            (currentRevocationPartition?.revocationDataBlob
                ?: "") + dccRevocationHash + dccRevocationExpirationTimestamp
        }

        val newRevocationPartition: DccRevocationPartition =
            currentRevocationPartition?.copy(revocationDataBlob = newHashSubString)
                ?: DccRevocationPartition(
                    kid,
                    dccRevocationHash[0],
                    dccRevocationHash[1],
                    newHashSubString
                )

        dccRevocationLocalDataSource.addOrUpdate(newRevocationPartition)
    }

    override fun add(kid: String, dccRevocationEntry: DccRevocationEntry) {
        add(
            kid,
            dccRevocationEntry.dccRevocationHash,
            dccRevocationEntry.dccRevocationExpirationDate
        )
    }

    override fun contains(kid: String, dccHash: String): Boolean {
        val currentRevocationPartition: DccRevocationPartition? =
            dccRevocationLocalDataSource.getBy(kid, dccHash[0], dccHash[1])

        val dccHashIndex: Int =
            currentRevocationPartition?.revocationDataBlob?.indexOf(dccHash) ?: -1
        if (dccHashIndex >= 0) {
            val dccRevocationExpirationTimestampStartIndex = dccHashIndex + SHA_256_STRING_LENGTH
            val dccRevocationExpirationTimestamp =
                currentRevocationPartition!!.revocationDataBlob.subSequence(
                    dccRevocationExpirationTimestampStartIndex,
                    dccRevocationExpirationTimestampStartIndex + LONG_STRING_LENGTH
                ).toString().toLong()
            val dccRevocationExpirationZonedDateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(dccRevocationExpirationTimestamp),
                ZoneId.systemDefault()
            )

            return ZonedDateTime.now().isBefore(dccRevocationExpirationZonedDateTime)
        }

        return false
    }

    override fun remove(kid: String, dccHash: String) {
        val currentRevocationPartition: DccRevocationPartition? =
            dccRevocationLocalDataSource.getBy(kid, dccHash[0], dccHash[1])

        val dccHashIndex: Int =
            currentRevocationPartition?.revocationDataBlob?.indexOf(dccHash) ?: -1
        if (dccHashIndex >= 0) {
            val newRevocationBlob = currentRevocationPartition!!.revocationDataBlob.removeRange(
                dccHashIndex,
                dccHashIndex + SHA_256_STRING_LENGTH + LONG_STRING_LENGTH
            )

            if (newRevocationBlob.isEmpty()) {
                dccRevocationLocalDataSource.remove(currentRevocationPartition)
            } else {
                dccRevocationLocalDataSource.addOrUpdate(
                    currentRevocationPartition.copy(
                        revocationDataBlob = newRevocationBlob
                    )
                )
            }
        }
    }
}