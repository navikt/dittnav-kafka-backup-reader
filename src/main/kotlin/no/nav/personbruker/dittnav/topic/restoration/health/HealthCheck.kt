package no.nav.personbruker.dittnav.topic.restoration.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
