package no.nav.personbruker.dittnav.topic.restoration.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import java.time.Instant

object AvroOppgaveObjectMother {

    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Oppgave til brukeren"

    fun createOppgave(lopenummer: Int): Oppgave {
        return createOppgave(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createOppgave(lopenummer: Int, fodselsnummer: String, text: String): Oppgave {
        return Oppgave(
            Instant.now().toEpochMilli(),
            fodselsnummer,
            "100$lopenummer",
            text,
            "https://nav.no/systemX/$lopenummer",
            4)
    }
}