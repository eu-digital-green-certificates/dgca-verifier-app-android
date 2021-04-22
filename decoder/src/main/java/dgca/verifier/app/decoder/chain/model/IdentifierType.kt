package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class IdentifierType(val value: String) {

    @JsonProperty("PP")
    PASSPORT("PP"),

    @JsonProperty("NN")
    NATIONAL_IDENTIFIER("NN"),

    @JsonProperty("CZ")
    CITIZENSHIP("CZ"),

    @JsonProperty("HC")
    HEALTH("HC");

    companion object {
        fun findByValue(value: String): IdentifierType {
            return values().firstOrNull { it.value == value } ?: NATIONAL_IDENTIFIER
        }
    }
}