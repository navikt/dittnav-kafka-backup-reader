package no.nav.personbruker.dittnav.topic.restoration.done

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.DONE
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class DoneEventRelay(
    private val eventProducer: KafkaProducerWrapper<Done>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchProcessorService<Done> {

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Done>) {
        eventsMetricsProbe.runWithMetrics(eventType = DONE) {
            val eventList = events.asWrapperList()

            incEventsAttempted(eventList.size)

            eventProducer.sendEvents(eventList)

            incEventsConfirmed(eventList.size)
        }
    }
}