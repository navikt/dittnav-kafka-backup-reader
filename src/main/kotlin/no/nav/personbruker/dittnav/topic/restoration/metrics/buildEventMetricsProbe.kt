package no.nav.personbruker.dittnav.topic.restoration.metrics

import no.nav.personbruker.dittnav.topic.restoration.config.Environment
import no.nav.personbruker.dittnav.topic.restoration.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.topic.restoration.metrics.influx.SensuClient

fun buildEventMetricsProbe(environment: Environment): EventMetricsProbe {
    val metricsReporter = resolveMetricsReporter(environment)
    return EventMetricsProbe(metricsReporter)
}

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.sensuHost == "" || environment.sensuHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuClient = SensuClient(environment.sensuHost, environment.sensuPort.toInt())
        InfluxMetricsReporter(sensuClient, environment)
    }
}
