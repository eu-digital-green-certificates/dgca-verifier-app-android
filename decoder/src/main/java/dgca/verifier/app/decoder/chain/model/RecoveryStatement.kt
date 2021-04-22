package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RecoveryStatement(

    @JsonProperty("dis")
    val disease: String,

    @JsonProperty("dat")
    val date: String,

    @JsonProperty("cou")
    val country: String
)