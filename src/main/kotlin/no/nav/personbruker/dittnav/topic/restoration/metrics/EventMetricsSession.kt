package no.nav.personbruker.dittnav.topic.restoration.metrics
import no.nav.personbruker.dittnav.topic.restoration.config.EventType

class EventMetricsSession(val eventType: EventType) {
    private var eventsHandled = 0
    private var eventsAttempted = 0
    private val startTime = System.nanoTime()

    fun incEventsAttempted(amount: Int) {
        eventsAttempted += amount
    }

    fun incEventsConfirmed(amount: Int) {
        eventsHandled += amount
    }

    fun getEventsConfirmed() = eventsHandled

    fun getEventsAttempted() = eventsAttempted

    fun timeElapsedSinceSessionStartNanos() = System.nanoTime() - startTime
}