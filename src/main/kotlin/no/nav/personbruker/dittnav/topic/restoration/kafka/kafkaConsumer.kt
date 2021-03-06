package no.nav.personbruker.dittnav.topic.restoration.kafka
import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.consumer.KafkaConsumer

fun <T> KafkaConsumer<Nokkel, T>.rollbackToLastCommitted() {
    val assignedPartitions = assignment()
    val partitionCommittedInfo = committed(assignedPartitions)
    partitionCommittedInfo.forEach { (partition, lastCommitted) ->
        seek(partition, lastCommitted.offset())
    }
}
