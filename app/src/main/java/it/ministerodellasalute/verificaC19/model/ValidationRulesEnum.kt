/*
 *  license-start
 *  
 *  Copyright (C) 2021 Ministero della Salute and all other contributors
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/


package it.ministerodellasalute.verificaC19.model

enum class ValidationRulesEnum(val value: String) {
    APP_MIN_VERSION("android"),
    RECOVERY_CERT_START_DAY("recovery_cert_start_day"),
    RECOVERY_CERT_END_DAY("recovery_cert_end_day"),
    MOLECULAR_TEST_START_HOUR("molecular_test_start_hours"),
    MOLECULAR_TEST_END_HOUR("molecular_test_end_hours"),
    RAPID_TEST_START_HOUR("rapid_test_start_hours"),
    RAPID_TEST_END_HOUR("rapid_test_end_hours"),
    VACCINE_START_DAY_NOT_COMPLETE("vaccine_start_day_not_complete"),
    VACCINE_END_DAY_NOT_COMPLETE("vaccine_end_day_not_complete"),
    VACCINE_START_DAY_COMPLETE("vaccine_start_day_complete"),
    VACCINE_END_DAY_COMPLETE("vaccine_end_day_complete")
}