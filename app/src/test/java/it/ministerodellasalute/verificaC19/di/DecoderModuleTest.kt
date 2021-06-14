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
 *  Created by climent on 6/14/21 11:13 AM
 */

package it.ministerodellasalute.verificaC19.di

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dgca.verifier.app.decoder.base45.DefaultBase45Service
import dgca.verifier.app.decoder.cbor.DefaultCborService
import dgca.verifier.app.decoder.compression.DefaultCompressorService
import dgca.verifier.app.decoder.cose.DefaultCoseService
import dgca.verifier.app.decoder.cose.VerificationCryptoService
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator
import io.mockk.MockKAnnotations
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DecoderModuleTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var module: DecoderModule

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        module = DecoderModule
    }

    @Test
    fun `test PrefixValidationService dependency`() {
        MatcherAssert.assertThat("PrefixValidationService",
            module.providePrefixValidationService(),
            CoreMatchers.instanceOf(DefaultPrefixValidationService::class.java))
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `test Base45Decoder dependency`() {
        MatcherAssert.assertThat("Base45Decoder",
            module.provideBase45Decoder(),
            CoreMatchers.instanceOf(DefaultBase45Service::class.java))
    }

    @Test
    fun `test CompressorService dependency`() {
        MatcherAssert.assertThat("CompressorService",
            module.provideCompressorService(),
            CoreMatchers.instanceOf(DefaultCompressorService::class.java))
    }

    @Test
    fun `test CoseService dependency`() {
        MatcherAssert.assertThat("CoseService",
            module.provideCoseService(),
            CoreMatchers.instanceOf(DefaultCoseService::class.java))
    }

    @Test
    fun `test SchemaValidator dependency`() {
        MatcherAssert.assertThat("SchemaValidator",
            module.provideSchemaValidator(),
            CoreMatchers.instanceOf(DefaultSchemaValidator::class.java))
    }

    @Test
    fun `test CborService dependency`() {
        MatcherAssert.assertThat("CborService",
            module.provideCborService(),
            CoreMatchers.instanceOf(DefaultCborService::class.java))
    }

    @Test
    fun `test CryptoService dependency`() {
        MatcherAssert.assertThat("CryptoService",
            module.provideCryptoService(),
            CoreMatchers.instanceOf(VerificationCryptoService::class.java))
    }

}