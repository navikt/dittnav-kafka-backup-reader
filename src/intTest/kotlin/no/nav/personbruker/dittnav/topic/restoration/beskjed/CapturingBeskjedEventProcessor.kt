package no.nav.personbruker.dittnav.topic.restoration.beskjed

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.BeskjedRecord
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchProcessorService
import org.apache.kafka.clients.consumer.ConsumerRecords

class CapturingBeskjedEventProcessor: EventBatchProcessorService<Beskjed> {

    private val lock = ReentrantLock()
    private val eventBuffer = ArrayList<BeskjedRecord>()

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Beskjed>) {
        val eventList = events.asWrapperList()

        lock.withLock {
            eventBuffer.addAll(eventList)
        }
    }

    fun getEvents() = lock.withLock {
        eventBuffer.map { it }
    }
}