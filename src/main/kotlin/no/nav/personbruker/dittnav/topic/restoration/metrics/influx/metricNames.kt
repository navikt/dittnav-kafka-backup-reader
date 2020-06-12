package no.nav.personbruker.dittnav.topic.restoration.metrics.influx

private const val METRIC_NAMESPACE = "dittnav.kafka.topic.backup.v1"

const val EVENTS_RELAYED = "$METRIC_NAMESPACE.events_relayed"
const val EVENTS_RETRIED = "$METRIC_NAMESPACE.events_retried"
