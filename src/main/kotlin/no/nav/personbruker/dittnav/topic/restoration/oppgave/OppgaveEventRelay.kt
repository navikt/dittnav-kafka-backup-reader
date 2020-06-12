package no.nav.personbruker.dittnav.topic.restoration.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.OPPGAVE
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class OppgaveEventRelay(
    private val eventProducer: KafkaProducerWrapper<Oppgave>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchRelayService<Oppgave> {

    override suspend fun relayEvents(events: ConsumerRecords<Nokkel, Oppgave>) {
        eventsMetricsProbe.runWithMetrics(eventType = OPPGAVE) {
            val eventList = events.asWrapperList()

            eventProducer.sendEvents(eventList)

            incEventsHandled(eventList.size)
        }
    }
}