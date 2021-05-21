
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