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
 *  Created by mykhailo.nester on 03/09/2021, 15:46
 */

package dgca.verifier.app.android.verification.detailed.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class DefaultQrCodeConverter(
    private val multiFormatWriter: MultiFormatWriter,
    private val barcodeEncoder: BarcodeEncoder
) : QrCodeConverter {

    override fun convertStringIntoQrCode(text: String, size: Int): Bitmap {
        val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
        return barcodeEncoder.createBitmap(bitMatrix)
    }
}