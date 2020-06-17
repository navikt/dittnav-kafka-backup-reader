package no.nav.personbruker.dittnav.topic.restoration.common

import no.nav.brukernotifikasjon.schemas.*

data class RecordKeyValueWrapper <T> (
    val key: Nokkel,
    val value: T
)