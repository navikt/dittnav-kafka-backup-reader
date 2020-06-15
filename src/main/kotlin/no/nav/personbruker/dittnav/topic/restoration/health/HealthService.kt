package no.nav.personbruker.dittnav.topic.restoration.health

import no.nav.personbruker.dittnav.topic.restoration.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    suspend fun getHealthChecks(): List<HealthStatus> {
        return listOf(
                applicationContext.beskjedConsumer.status(),
                applicationContext.oppgaveConsumer.status(),
                applicationContext.doneConsumer.status()
        )
    }
}