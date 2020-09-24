package no.nav.personbruker.dittnav.topic.restoration.done

import no.nav.brukernotifikasjon.schemas.Done
import java.time.Instant

object AvroDoneObjectMother {

    private val defaultFodselsnr = "12345"

    fun createDone(lopenummer: Int): Done {
        return createDone(lopenummer, defaultFodselsnr)
    }

    fun createDone(lopenummer: Int, fodselsnummer: String): Done {
        return Done(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer")
    }
}