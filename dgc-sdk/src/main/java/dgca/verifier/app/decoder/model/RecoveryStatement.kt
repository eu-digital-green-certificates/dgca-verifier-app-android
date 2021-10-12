/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by mykhailo.nester on 4/30/21 1:59 PM
 */

package dgca.verifier.app.decoder.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime
import java.io.Serializable
import java.time.*
import java.time.format.DateTimeFormatter

data class RecoveryStatement(

    @JsonProperty("tg")
    val disease: String,

    @JsonProperty("fr")
    val dateOfFirstPositiveTest: String,

    @JsonProperty("co")
    val countryOfVaccination: String,

    @JsonProperty("is")
    val certificateIssuer: String,

    @JsonProperty("df")
    val certificateValidFrom: String,

    @JsonProperty("du")
    val certificateValidUntil: String,

    @JsonProperty("ci")
    val certificateIdentifier: String

) : Serializable {
    companion object {
        private val UTC_ZONE_ID: ZoneId = ZoneId.ofOffset("", ZoneOffset.UTC).normalized()
    }

    fun isCertificateNotValidAnymore(): Boolean? =
        certificateValidUntil.toZonedDateTimeOrUtcLocal()?.isBefore(ZonedDateTime.now())

    fun isCertificateNotValidSoFar(): Boolean? =
        certificateValidFrom.toZonedDateTimeOrUtcLocal()?.isAfter(ZonedDateTime.now())

    private fun String.toZonedDateTime(): ZonedDateTime? = try {
        ZonedDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toLocalDateTime(): LocalDateTime? = try {
        LocalDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toLocalDate(): LocalDate? = try {
        LocalDate.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toZonedDateTimeOrUtcLocal(): ZonedDateTime? =
        this.toZonedDateTime()?.withZoneSameInstant(UTC_ZONE_ID) ?: this.toLocalDateTime()
            ?.atZone(UTC_ZONE_ID) ?: this.toLocalDate()?.atStartOfDay(UTC_ZONE_ID)
}