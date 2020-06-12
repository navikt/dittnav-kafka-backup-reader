package no.nav.personbruker.dittnav.topic.restoration.metrics
import no.nav.personbruker.dittnav.topic.restoration.config.EventType

class EventMetricsSession(val eventType: EventType) {
    private var eventsHandled = 0
    private var eventsRetried = 0
    private val startTime = System.nanoTime()

    fun incEventsRetried(amount: Int) {
        eventsRetried += amount
    }

    fun incEventsHandled(amount: Int) {
        eventsHandled += amount
    }

    fun getEventsHandled() = eventsHandled

    fun getEventsRetried() = eventsRetried

    fun timeElapsedSinceSessionStartNanos() = System.nanoTime() - startTime
}