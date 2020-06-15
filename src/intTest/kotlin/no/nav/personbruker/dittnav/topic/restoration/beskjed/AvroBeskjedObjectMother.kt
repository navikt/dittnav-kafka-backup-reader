package no.nav.personbruker.dittnav.topic.restoration.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import java.time.Instant

object AvroBeskjedObjectMother {

    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Beskjed til brukeren"

    fun createBeskjed(lopenummer: Int): Beskjed {
        return createBeskjed(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createBeskjed(lopenummer: Int, fodselsnummer: String, text: String): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4)
    }
}