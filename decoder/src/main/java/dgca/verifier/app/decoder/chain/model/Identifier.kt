package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Identifier(

    @JsonProperty("t")
    val type: IdentifierType,

    @JsonProperty("i")
    val id: String,

    @JsonProperty("c")
    val country: String? = null
)