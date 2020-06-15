package no.nav.personbruker.dittnav.topic.restoration.kafka

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.RecordKeyValueWrapper
import no.nav.personbruker.dittnav.topic.restoration.common.exception.RetriableKafkaException
import no.nav.personbruker.dittnav.topic.restoration.common.exception.UnretriableKafkaException
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import org.slf4j.LoggerFactory

class KafkaProducerWrapper<T>(
    private val destinationTopicName: String,
    private val kafkaProducer: KafkaProducer<Nokkel, T>
) {

    private val log = LoggerFactory.getLogger(KafkaProducerWrapper::class.java)

    fun sendEvents(events: List<RecordKeyValueWrapper<T>>) {
        try {
            kafkaProducer.beginTransaction()
            events.forEach { event ->
                sendEvent(event)
            }
            kafkaProducer.commitTransaction()
        } catch (e: KafkaException) {
            kafkaProducer.abortTransaction()
            throw RetriableKafkaException("Et eller flere eventer feilet med en periodisk feil ved sending til kafka", e)
        } catch (e: Exception) {
            kafkaProducer.close()
            throw UnretriableKafkaException("Fant en uventet feil ved sending av eventer til kafka", e)
        }
    }

    private fun sendEvent(event: RecordKeyValueWrapper<T>) {
        val producerRecord = ProducerRecord(destinationTopicName, event.key, event.value)
        kafkaProducer.send(producerRecord)
    }

    fun flushAndClose() {
        try {
            kafkaProducer.flush()
            kafkaProducer.close()
            log.info("Produsent for kafka-eventer er flushet og lukket.")
        } catch (e: Exception) {
            log.warn("Klarte ikke å flushe og lukke produsent. Det kan være eventer som ikke ble produsert.")
        }
    }
}