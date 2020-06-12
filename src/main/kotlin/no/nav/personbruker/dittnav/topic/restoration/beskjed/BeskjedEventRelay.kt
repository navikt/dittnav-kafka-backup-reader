package no.nav.personbruker.dittnav.topic.restoration.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.BESKJED
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class BeskjedEventRelay(
    private val eventProducer: KafkaProducerWrapper<Beskjed>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchRelayService<Beskjed> {

    override suspend fun relayEvents(events: ConsumerRecords<Nokkel, Beskjed>) {
        eventsMetricsProbe.runWithMetrics(eventType = BESKJED) {
            val eventList = events.asWrapperList()

            eventProducer.sendEvents(eventList)

            incEventsHandled(eventList.size)
        }
    }
}