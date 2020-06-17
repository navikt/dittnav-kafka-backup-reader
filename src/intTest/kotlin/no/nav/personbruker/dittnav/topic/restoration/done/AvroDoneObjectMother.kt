package no.nav.personbruker.dittnav.topic.restoration.done

import no.nav.brukernotifikasjon.schemas.Done
import java.time.Instant

object AvroDoneObjectMother {

    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Done til brukeren"

    fun createDone(lopenummer: Int): Done {
        return createDone(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createDone(lopenummer: Int, fodselsnummer: String, text: String): Done {
        return Done(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer")
    }
}