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
 *  Created by mykhailo.nester on 17/08/2021, 18:52
 */

package dgca.verifier.app.android.inputrecognizer.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.experimental.and

object NdefParser {

    fun parse(message: NdefMessage): List<ParsedNdefRecord> = getRecords(message.records)

    private fun getRecords(records: Array<NdefRecord>): List<ParsedNdefRecord> =
        records.map {
            it.parse() ?: object : ParsedNdefRecord {
                override fun str(): String {
                    return String(it.payload)
                }
            }
        }
}

fun NdefRecord.parse(): ParsedNdefRecord? {
    return if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type, NdefRecord.RTD_TEXT)) {
        try {
            val recordPayload = payload

            /*
             * payload[0] contains the "Status Byte Encodings" field, per the
             * NFC Forum "Text Record Type Definition" section 3.2.1.
             *
             * bit7 is the Text Encoding Field.
             *
             * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
             * The text is encoded in UTF16
             *
             * Bit_6 is reserved for future use and must be set to zero.
             */
            val textEncoding = if (recordPayload[0] and 128.toByte() == 0.toByte()) {
                Charsets.UTF_8
            } else {
                Charsets.UTF_16
            }

            val langCodeLen = (recordPayload[0] and 63.toByte()).toInt()
            val text = String(recordPayload, textEncoding).substring(1 + langCodeLen)

            return TextRecord(text)
        } catch (e: UnsupportedEncodingException) {
            Timber.w("We got a malformed tag.")
            return null
        }
    } else {
        null
    }
}
