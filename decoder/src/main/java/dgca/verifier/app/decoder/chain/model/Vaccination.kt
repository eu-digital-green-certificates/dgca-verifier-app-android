package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Vaccination(

    @JsonProperty("dis")
    val disease: String,

    @JsonProperty("vap")
    val vaccine: String,

    @JsonProperty("mep")
    val medicinalProduct: String,

    @JsonProperty("aut")
    val authorizationHolder: String,

    @JsonProperty("seq")
    val doseSequence: Int,

    @JsonProperty("tot")
    val doseTotalNumber: Int,

    @JsonProperty("lot")
    val lotNumber: String? = null,

    @JsonProperty("dat")
    val date: String,

    @JsonProperty("adm")
    val administeringCentre: String? = null,

    @JsonProperty("cou")
    val country: String
): Serializable