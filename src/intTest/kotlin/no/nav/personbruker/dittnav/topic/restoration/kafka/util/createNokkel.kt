package no.nav.personbruker.dittnav.topic.restoration.kafka.util

import no.nav.brukernotifikasjon.schemas.Nokkel

fun createNokkel(eventId: Int): Nokkel = Nokkel("dummySystembruker", eventId.toString())