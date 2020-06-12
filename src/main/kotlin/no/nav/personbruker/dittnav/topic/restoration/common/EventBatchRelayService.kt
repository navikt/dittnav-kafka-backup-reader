package no.nav.personbruker.dittnav.topic.restoration.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchRelayService<T> {

    suspend fun relayEvents(events: ConsumerRecords<Nokkel, T>)

    val ConsumerRecord<Nokkel, T>.systembruker : String get() = key().getSystembruker()

    fun ConsumerRecords<Nokkel, T>.asWrapperList() : List<RecordKeyValueWrapper<T>> = map { record ->
        RecordKeyValueWrapper(record.key(), record.value())
    }


}