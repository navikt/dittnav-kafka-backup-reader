package no.nav.personbruker.dittnav.topic.restoration.done

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.builders.DoneBuilder
import java.time.LocalDateTime

object AvroDoneObjectMother {

    private val defaultFodselsnr = "12345678901"

    fun createDone(lopenummer: Int): Done {
        return createDone(lopenummer, defaultFodselsnr)
    }

    fun createDone(lopenummer: Int, fodselsnummer: String): Done {
        return DoneBuilder()
                .withTidspunkt(LocalDateTime.now())
                .withGrupperingsId("100$lopenummer")
                .withFodselsnummer(fodselsnummer)
                .build()
    }
}
