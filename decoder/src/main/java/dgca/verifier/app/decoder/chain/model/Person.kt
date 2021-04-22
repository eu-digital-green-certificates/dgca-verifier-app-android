package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Person(

    @JsonProperty("gn")
    val givenName: String,

    @JsonProperty("gnt")
    val givenNameTransliterated: String? = null,

    @JsonProperty("fn")
    val familyName: String? = null,

    @JsonProperty("fnt")
    val familyNameTransliterated: String? = null,

    @JsonProperty("id")
    val identifiers: List<Identifier?>? = null,

    @JsonProperty("dob")
    val dateOfBirth: String,
)