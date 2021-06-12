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
 *  Created by climent on 6/7/21 6:14 PM
 */

package it.ministerodellasalute.verificaC19.utils.mock

import it.ministerodellasalute.verificaC19.data.remote.model.Rule

class ServiceMocks {
    companion object {

        private const val VERIFICATION_RULES_SUCCESS = "verification_rules_success_response.json"
        private const val QR_CODE_EXAMPLE = "qr_code.txt"
        private const val QR_CODE_PLAIN_INPUT = "qr_code_plain_input.txt"
        private const val QR_CODE_COMPRESSED_COSE = "qr_code_compressed_cose.txt"
        private const val QR_CODE_COSE = "qr_code_cose.txt"
        private const val QR_CODE_KID = "qr_code_kid.txt"
        private const val QR_CODE_CBOR = "qr_code_cbor.txt"
        private const val QR_CODE_CERTIFICATE = "qr_code_certificate.txt"

        @JvmStatic
        fun getVerificationRulesSuccessResponse(): Array<Rule>{
            val systemResponse = MockDataUtils.GSON.fromJson(
                MockDataUtils.readFile(VERIFICATION_RULES_SUCCESS), Array<Rule>::class.java)
            return systemResponse
        }

        @JvmStatic
        fun getVerificationRulesStringResponse(): String{
            return MockDataUtils.readFile(VERIFICATION_RULES_SUCCESS)
        }

        @JvmStatic
        fun getQrCode(): String{
            return MockDataUtils.readFile(QR_CODE_EXAMPLE)
        }

        @JvmStatic
        fun getQrCodePlainInput(): String{
            return MockDataUtils.readFile(QR_CODE_PLAIN_INPUT)
        }

        @JvmStatic
        fun getQrCodeCompressedCose(): String{
            return MockDataUtils.readFile(QR_CODE_COMPRESSED_COSE)
        }

        @JvmStatic
        fun getQrCodeCose(): String{
            return MockDataUtils.readFile(QR_CODE_COSE)
        }

        @JvmStatic
        fun getQrCodeKid(): String{
            return MockDataUtils.readFile(QR_CODE_KID)
        }

        @JvmStatic
        fun getQrCodeCbor(): String{
            return MockDataUtils.readFile(QR_CODE_CBOR)
        }

        @JvmStatic
        fun getQrCodeCertificate(): String{
            return MockDataUtils.readFile(QR_CODE_CERTIFICATE)
        }

    }
}