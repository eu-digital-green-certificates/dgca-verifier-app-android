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
 *  Created by osarapulov on 3/17/22, 2:48 PM
 */

package dgca.verifier.app.android.diia.di

import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dgca.verifier.app.android.diia.ui.verification.detailed.qr.DefaultQrCodeConverter
import dgca.verifier.app.android.diia.ui.verification.detailed.qr.QrCodeConverter
import javax.inject.Singleton

/**
 * Provide QR decoder functionality for injection.
 */
@ExperimentalUnsignedTypes
@InstallIn(SingletonComponent::class)
@Module
object QrCodeModule {

    @Singleton
    @Provides
    fun provideMultiFormatWriter(): MultiFormatWriter = MultiFormatWriter()

    @Singleton
    @Provides
    fun provideBarcodeEncoder(): BarcodeEncoder = BarcodeEncoder()

    @Singleton
    @Provides
    fun provideQrCodeConverter(
        multiFormatWriter: MultiFormatWriter,
        barcodeEncoder: BarcodeEncoder
    ): QrCodeConverter = DefaultQrCodeConverter(multiFormatWriter, barcodeEncoder)
}
