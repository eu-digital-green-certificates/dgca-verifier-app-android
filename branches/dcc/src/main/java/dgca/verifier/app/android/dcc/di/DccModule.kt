/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 3/17/22, 1:46 PM
 */

package dgca.verifier.app.android.dcc.di

import android.content.Context
import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import dgca.verifier.app.android.dcc.DccProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dgca.verifier.app.android.dcc.DccProcessor
import dagger.multibindings.IntoSet
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.base45.DefaultBase45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.DefaultCborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.compression.DefaultCompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.cose.DefaultCoseService
import dgca.verifier.app.decoder.cose.VerificationCryptoService
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator
import dgca.verifier.app.decoder.schema.SchemaValidator
import dgca.verifier.app.decoder.services.X509
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DccModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideDccProcessor(
        @ApplicationContext context: Context,
        validationService: PrefixValidationService,
        base45Service: Base45Service,
        compressorService: CompressorService,
        coseService: CoseService
    ): Processor = DccProcessor(
        context,
        validationService,
        base45Service,
        compressorService,
        coseService
    )

    @Singleton
    @Provides
    fun providePrefixValidationService(): PrefixValidationService = DefaultPrefixValidationService()

    @ExperimentalUnsignedTypes
    @Singleton
    @Provides
    fun provideBase45Decoder(): Base45Service = DefaultBase45Service()

    @Singleton
    @Provides
    fun provideCompressorService(): CompressorService = DefaultCompressorService()

    @Singleton
    @Provides
    fun provideCoseService(): CoseService = DefaultCoseService()

    @Singleton
    @Provides
    fun provideSchemaValidator(): SchemaValidator = DefaultSchemaValidator()

    @Singleton
    @Provides
    fun provideCborService(): CborService = DefaultCborService()

    @Singleton
    @Provides
    fun provideX509(): X509 = X509()

    @Singleton
    @Provides
    fun provideCryptoService(x509: X509): CryptoService = VerificationCryptoService(x509)
}