package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Test(

    @JsonProperty("dis")
    val disease: String,

    @JsonProperty("typ")
    val type: String,

    @JsonProperty("tma")
    val manufacturer: String? = null,

    @JsonProperty("ori")
    val sampleOrigin: String? = null,

    @JsonProperty("dts")
    val dateTimeSample: String,

    @JsonProperty("dtr")
    val dateTimeResult: String,

    @JsonProperty("res")
    val resultPositive: Boolean,

    @JsonProperty("fac")
    val testFacility: String,

    @JsonProperty("cou")
    val country: String
) : Serializable