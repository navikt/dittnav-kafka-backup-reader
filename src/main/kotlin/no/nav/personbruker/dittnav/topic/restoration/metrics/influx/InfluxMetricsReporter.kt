package no.nav.personbruker.dittnav.topic.restoration.metrics.influx

import no.nav.personbruker.dittnav.topic.restoration.config.Environment
import no.nav.personbruker.dittnav.topic.restoration.metrics.MetricsReporter
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

class InfluxMetricsReporter(val sensuClient: SensuClient, environment: Environment) : MetricsReporter {

    override suspend fun registerDataPoint(measurement: String, fields: Map<String, Any>, tags: Map<String, String>) {
        val point = Point.measurement(measurement)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag(tags)
                .tag(DEFAULT_TAGS)
                .fields(fields)
                .build()

        sensuClient.submitEvent(SensuEvent(point))
    }

    private val DEFAULT_TAGS = listOf(
        "application" to "dittnav-event-aggregator",
        "cluster" to environment.clusterName,
        "namespace" to environment.namespace
    ).toMap()
}