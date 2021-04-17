package dgca.verifier.app.android.chain

import kotlinx.serialization.Serializable

@Serializable
data class VaccinationData(
    val sub: Person? = null,
    val vac: List<Vaccination?>? = null,
    val rec: List<PastInfection?>? = null,
    val tst: List<Test?>? = null,
    val cert: DocumentMetadata? = null
)

@Serializable
data class Person(
    val n: String? = null,
    val gn: String? = null,
    val fn: String? = null,
    val dob: String? = null,
    val gen: String? = null,
    val id: List<Identifier?>? = null
)

@Serializable
data class Identifier(
    val t: String? = null,
    val i: String? = null
)

@Serializable
data class Vaccination(
    val dis: String? = null,
    val des: String? = null,
    val nam: String? = null,
    val vap: String? = null,
    val mep: String? = null,
    val aut: String? = null,
    val seq: Int? = null,
    val tot: Int? = null,
    val lot: String? = null,
    val dat: String? = null,
    val adm: String? = null,
    val cou: String? = null
)


@Serializable
data class PastInfection(
    val dis: String? = null,
    val dat: String? = null,
    val cou: String? = null
)

@Serializable
data class DocumentMetadata(
    val `is`: String? = null,
    val id: String? = null,
    val vf: String? = null,
    val vu: String? = null,
    val co: String? = null,
    val vr: String? = null
)

@Serializable
data class Test(
    val dis: String? = null,
    val typ: String? = null,
    val tna: String? = null,
    val tma: String? = null,
    val ori: String? = null,
    val dat: String? = null,
    val dts: String? = null,
    val dtr: String? = null,
    val res: String? = null,
    val fac: String? = null,
    val cou: String? = null
)


