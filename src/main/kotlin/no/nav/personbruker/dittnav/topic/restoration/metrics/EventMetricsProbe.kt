package no.nav.personbruker.dittnav.topic.restoration.metrics

import no.nav.personbruker.dittnav.topic.restoration.config.EventType
import no.nav.personbruker.dittnav.topic.restoration.metrics.influx.EVENTS_RELAYED

class EventMetricsProbe(private val metricsReporter: MetricsReporter) {

    suspend fun runWithMetrics(eventType: EventType, block: suspend EventMetricsSession.() -> Unit) {
        val session = EventMetricsSession(eventType)
        try {
            block.invoke(session)
        } finally {
            val processingTime = session.timeElapsedSinceSessionStartNanos()

            if (session.getEventsAttempted() > 0) {
                handleEventsBatch(session, processingTime)
            }
        }
    }

    private suspend fun handleEventsBatch(session: EventMetricsSession, processingTime: Long) {
        val fieldMap = listOf(
                "events_sent_successfully" to session.getEventsConfirmed(),
                "events_attempted" to session.getEventsAttempted(),
                "processingTime" to processingTime
        ).toMap()

        val tagMap = listOf("eventType" to session.eventType.toString()).toMap()

        metricsReporter.registerDataPoint(EVENTS_RELAYED, fieldMap, tagMap)
    }
}