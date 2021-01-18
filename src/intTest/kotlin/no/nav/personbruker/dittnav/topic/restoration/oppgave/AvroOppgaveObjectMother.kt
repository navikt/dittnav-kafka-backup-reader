package no.nav.personbruker.dittnav.topic.restoration.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.brukernotifikasjon.schemas.builders.OppgaveBuilder
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime

object AvroOppgaveObjectMother {

    private val defaultFodselsnr = "12345678901"
    private val defaultText = "Dette er Oppgave til brukeren"

    fun createOppgave(lopenummer: Int): Oppgave {
        return createOppgave(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createOppgave(lopenummer: Int, fodselsnummer: String, text: String): Oppgave {
        return OppgaveBuilder()
                .withTidspunkt(LocalDateTime.now())
                .withFodselsnummer(fodselsnummer)
                .withGrupperingsId("100$lopenummer")
                .withTekst(text)
                .withLink(URL("https://nav.no/systemX/$lopenummer"))
                .withSikkerhetsnivaa(4)
                .build()
    }
}
