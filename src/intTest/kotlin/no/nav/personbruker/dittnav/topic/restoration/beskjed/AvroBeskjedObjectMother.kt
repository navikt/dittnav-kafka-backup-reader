package no.nav.personbruker.dittnav.topic.restoration.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.builders.BeskjedBuilder
import java.net.URL
import java.time.LocalDateTime

object AvroBeskjedObjectMother {

    private val defaultFodselsnr = "12345678901"
    private val defaultText = "Dette er Beskjed til brukeren"

    fun createBeskjed(lopenummer: Int): Beskjed {
        return createBeskjed(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createBeskjed(lopenummer: Int, fodselsnummer: String, text: String): Beskjed {
        return BeskjedBuilder()
                .withTidspunkt(LocalDateTime.now())
                .withFodselsnummer(fodselsnummer)
                .withGrupperingsId("100$lopenummer")
                .withTekst(text)
                .withLink(URL("https://nav.no/systemX/$lopenummer"))
                .withSikkerhetsnivaa(4)
                .build()
    }
}
