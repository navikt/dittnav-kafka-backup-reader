package no.nav.personbruker.dittnav.topic.restoration.done

import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.DONE
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class DoneEventRelay(
    private val eventProducer: KafkaProducerWrapper<Done>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchRelayService<Done> {

    override suspend fun relayEvents(events: ConsumerRecords<Nokkel, Done>) {
        eventsMetricsProbe.runWithMetrics(eventType = DONE) {
            val eventList = events.asWrapperList()

            eventProducer.sendEvents(eventList)

            incEventsHandled(eventList.size)
        }
    }
}