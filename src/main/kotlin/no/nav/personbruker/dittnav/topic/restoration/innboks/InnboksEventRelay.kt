package no.nav.personbruker.dittnav.topic.restoration.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.INNBOKS
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class InnboksEventRelay(
    private val eventProducer: KafkaProducerWrapper<Innboks>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchRelayService<Innboks> {

    override suspend fun relayEvents(events: ConsumerRecords<Nokkel, Innboks>) {
        eventsMetricsProbe.runWithMetrics(eventType = INNBOKS) {
            val eventList = events.asWrapperList()

            eventProducer.sendEvents(eventList)

            incEventsHandled(eventList.size)
        }
    }
}