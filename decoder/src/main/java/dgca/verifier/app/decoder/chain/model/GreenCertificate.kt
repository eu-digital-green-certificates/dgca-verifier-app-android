package dgca.verifier.app.decoder.chain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class GreenCertificate(

    @JsonProperty("sub")
    val subject: Person,

    @JsonProperty("v")
    val schemaVersion: String,

    @JsonProperty("dgcid")
    val identifier: String,

    @JsonProperty("vac")
    val vaccinations: List<Vaccination> = listOf(),

    @JsonProperty("rec")
    val recoveryStatements: List<RecoveryStatement> = listOf(),

    @JsonProperty("tst")
    val tests: List<Test> = listOf(),
) : Serializable