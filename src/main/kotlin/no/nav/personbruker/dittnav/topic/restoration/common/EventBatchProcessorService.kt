package no.nav.personbruker.dittnav.topic.restoration.common

import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords

interface EventBatchProcessorService<T> {

    suspend fun processEvents(events: ConsumerRecords<Nokkel, T>)

    fun ConsumerRecords<Nokkel, T>.asWrapperList() : List<RecordKeyValueWrapper<T>> = map { record ->
        RecordKeyValueWrapper(record.key(), record.value())
    }


}