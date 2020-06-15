package no.nav.personbruker.dittnav.topic.restoration.innboks


import no.nav.brukernotifikasjon.schemas.Innboks
import java.time.Instant

object AvroInnboksObjectMother {

    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Innboks til brukeren"

    fun createInnboks(lopenummer: Int): Innboks {
        return createInnboks(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createInnboks(lopenummer: Int, fodselsnummer: String, text: String): Innboks {
        return Innboks(
            Instant.now().toEpochMilli(),
            fodselsnummer,
            "100$lopenummer",
            text,
            "https://nav.no/systemX/$lopenummer",
            4)
    }
}