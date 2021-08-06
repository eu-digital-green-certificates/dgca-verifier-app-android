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
 *  Created by climent on 6/7/21 6:46 PM
 */

package it.ministerodellasalute.verificaC19.ui.main.verification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.model.CoseData
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.SchemaValidator
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import it.ministerodellasalute.verificaC19.data.VerifierRepository
import it.ministerodellasalute.verificaC19.data.local.Preferences
import it.ministerodellasalute.verificaC19.di.DispatcherProvider
import it.ministerodellasalute.verificaC19.utils.Base64
import it.ministerodellasalute.verificaC19.utils.MainCoroutineScopeRule
import it.ministerodellasalute.verificaC19.utils.mock.ServiceMocks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


class VerificationViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineScopeRule: MainCoroutineScopeRule = MainCoroutineScopeRule()

    @RelaxedMockK
    private lateinit var prefixValidationService: PrefixValidationService

    @RelaxedMockK
    private lateinit var base45Service: Base45Service

    @RelaxedMockK
    private lateinit var compressorService: CompressorService

    @RelaxedMockK
    private lateinit var cryptoService: CryptoService

    @RelaxedMockK
    private lateinit var coseService: CoseService

    @RelaxedMockK
    private lateinit var schemaValidator: SchemaValidator

    @RelaxedMockK
    private lateinit var cborService: CborService

    @RelaxedMockK
    private lateinit var verifierRepository: VerifierRepository

    @RelaxedMockK
    private lateinit var preferences: Preferences

    private lateinit var viewModel: VerificationViewModel

    @RelaxedMockK
    private val dispatcherProvider: DispatcherProvider = mockk()


    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every{dispatcherProvider.getIO()}.returns(mainCoroutineScopeRule.testDispatcher)

        viewModel = VerificationViewModel(prefixValidationService, base45Service, compressorService,
            cryptoService, coseService, schemaValidator, cborService, verifierRepository, preferences, dispatcherProvider)
    }

    @Before
    fun `Bypass android_util_Base64 to java_util_Base64`() {
        mockkStatic(android.util.Base64::class)
        val arraySlot = slot<ByteArray>()
        every {
            android.util.Base64.encodeToString(capture(arraySlot), android.util.Base64.NO_WRAP)
        } answers {
            java.util.Base64.getEncoder().encodeToString(arraySlot.captured)
        }

        val stringSlot = slot<String>()
        every {
            android.util.Base64.decode(capture(stringSlot), android.util.Base64.NO_WRAP)
        } answers {
            java.util.Base64.getDecoder().decode(stringSlot.captured)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `test decode function`() = mainCoroutineScopeRule.runBlockingTest {
        val qrCode = ServiceMocks.getQrCode()
        val plainInput = ServiceMocks.getQrCodePlainInput()
        val compressedCose = ServiceMocks.getQrCodeCompressedCose()
        val cose = ServiceMocks.getQrCodeCose()

        val kid = ServiceMocks.getQrCodeKid()
        val cbor = ServiceMocks.getQrCodeCbor()

        val caseData = CoseData(cbor.toByteArray(), kid.toByteArray())

        val certificate = ServiceMocks.getQrCodeCertificate()

        val mockObserver = mockk<Observer<Boolean>>()
        val slot = slot<Boolean>()
        val listOfResponse = arrayListOf<Boolean>()

        every{prefixValidationService.decode(any(), any())}.returns(plainInput)
        every{base45Service.decode(any(), any())}.returns(compressedCose.toByteArray())
        every{compressorService.decode(any(), any())}.returns(cose.toByteArray())
        every{coseService.decode(any(), any())}.returns(caseData)

        coEvery{verifierRepository.getCertificate(any())}.returns(certificate.base64ToX509Certificate() as Certificate)

        every { mockObserver.onChanged(capture(slot)) } answers {
            listOfResponse.add(slot.captured)
        }
        viewModel.inProgress.observeForever(mockObserver)

        viewModel.decode(qrCode)

        assertNotEquals(viewModel.certificate.value, null)
        assertEquals(true, listOfResponse[0])
        assertEquals(false, listOfResponse[1])
    }

    @Test
    fun `getRecoveryCertStartDay`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getRecoveryCertStartDay()

        assertEquals(expectedData, "0")
    }

    @Test
    fun `getRecoveryCertEndDay`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getRecoveryCertEndDay()

        assertEquals(expectedData, "180")
    }

    @Test
    fun `getRapidTestStartHour`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getRapidTestStartHour()

        assertEquals(expectedData, "0")
    }

    @Test
    fun `getRapidTestEndHour`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getRapidTestEndHour()

        assertEquals(expectedData, "48")
    }

    @Test
    fun `getVaccineStartDayNotComplete for EU-1-20-1528 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayNotComplete("EU/1/20/1528")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayNotComplete for EU-1-20-1528 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayNotComplete("EU/1/20/1528")

        assertEquals(expectedData, "42")
    }

    @Test
    fun `getVaccineStartDayComplete for EU-1-20-1528 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayComplete("EU/1/20/1528")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayComplete for EU-1-20-1528 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayComplete("EU/1/20/1528")

        assertEquals(expectedData, "270")
    }

    @Test
    fun `getVaccineStartDayNotComplete for EU-1-20-1507 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayNotComplete("EU/1/20/1507")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayNotComplete for EU-1-20-1507 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayNotComplete("EU/1/20/1507")

        assertEquals(expectedData, "42")
    }

    @Test
    fun `getVaccineStartDayComplete for EU-1-20-1507 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayComplete("EU/1/20/1507")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayComplete for EU-1-20-1507 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayComplete("EU/1/20/1507")

        assertEquals(expectedData, "270")
    }

    @Test
    fun `getVaccineStartDayNotComplete for EU-1-21-1529 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayNotComplete("EU/1/21/1529")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayNotComplete for EU-1-21-1529 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayNotComplete("EU/1/21/1529")

        assertEquals(expectedData, "84")
    }

    @Test
    fun `getVaccineStartDayComplete for EU-1-21-1529 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayComplete("EU/1/21/1529")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayComplete for EU-1-21-1529 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayComplete("EU/1/21/1529")

        assertEquals(expectedData, "270")
    }

    @Test
    fun `getVaccineStartDayNotComplete for EU-1-20-1525 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayNotComplete("EU/1/20/1525")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayNotComplete for EU-1-20-1525 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayNotComplete("EU/1/20/1525")

        assertEquals(expectedData, "270")
    }

    @Test
    fun `getVaccineStartDayComplete for EU-1-20-1525 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData = viewModel.getVaccineStartDayComplete("EU/1/20/1525")

        assertEquals(expectedData, "15")
    }

    @Test
    fun `getVaccineEndDayComplete for EU-1-20-1525 vaccin type`() {
        val response = ServiceMocks.getVerificationRulesStringResponse()
        every { preferences.validationRulesJson }.returns(response)

        val expectedData =  viewModel.getVaccineEndDayComplete("EU/1/20/1525")

        assertEquals(expectedData, "270")
    }

    private fun String.base64ToX509Certificate(): X509Certificate? {
        val decoded = Base64.decode(this, 2)
        val inputStream = ByteArrayInputStream(decoded)
        return CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as? X509Certificate
    }

}