package no.nav.personbruker.dittnav.topic.restoration.common.exception

class RetriableKafkaException(message: String, cause: Throwable?) : AbstractPersonbrukerException(message, cause) {
    constructor(message: String) : this(message, null)
}
