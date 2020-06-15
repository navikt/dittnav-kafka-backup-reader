package no.nav.personbruker.dittnav.topic.restoration.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.topic.restoration.config.EventType.OPPGAVE
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import org.apache.kafka.clients.consumer.ConsumerRecords

class OppgaveEventRelay(
    private val eventProducer: KafkaProducerWrapper<Oppgave>,
    private val eventsMetricsProbe: EventMetricsProbe
): EventBatchProcessorService<Oppgave> {

    override suspend fun processEvents(events: ConsumerRecords<Nokkel, Oppgave>) {
        eventsMetricsProbe.runWithMetrics(eventType = OPPGAVE) {
            val eventList = events.asWrapperList()

            incEventsAttempted(eventList.size)

            eventProducer.sendEvents(eventList)

            incEventsConfirmed(eventList.size)
        }
    }
}