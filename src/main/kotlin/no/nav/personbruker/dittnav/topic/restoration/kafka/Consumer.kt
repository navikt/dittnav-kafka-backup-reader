package no.nav.personbruker.dittnav.topic.restoration.kafka

import kotlinx.coroutines.*
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
import no.nav.personbruker.dittnav.topic.restoration.common.exception.RetriableKafkaException
import no.nav.personbruker.dittnav.topic.restoration.common.exception.UnretriableKafkaException
import no.nav.personbruker.dittnav.topic.restoration.health.HealthCheck
import no.nav.personbruker.dittnav.topic.restoration.health.HealthStatus
import no.nav.personbruker.dittnav.topic.restoration.health.Status
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.coroutines.CoroutineContext

class Consumer<T>(
    val topic: String,
    val kafkaConsumer: KafkaConsumer<Nokkel, T>,
    val eventBatchRelayService: EventBatchRelayService<T>,
    val job: Job = Job(),
    var neverStarted: Boolean = true
) : CoroutineScope, HealthCheck {

    private val log: Logger = LoggerFactory.getLogger(Consumer::class.java)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun stopPolling() {
        job.cancelAndJoin()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    override suspend fun status(): HealthStatus {
        val serviceName = topic + "consumer"
        return if (neverStarted) {
            log.info("Selftest uklar, kafka consumere har ikke blitt startet enda.")
            HealthStatus(serviceName, Status.NOT_STARTED, "Consumer is not running", includeInReadiness = false)
        } else if (job.isActive) {
            HealthStatus(serviceName, Status.OK, "Consumer is running", includeInReadiness = false)
        } else {
            log.error("Selftest mot Kafka-consumere , consumer kjører ikke.")
            HealthStatus(serviceName, Status.ERROR, "Consumer is not running", includeInReadiness = false)
        }
    }

    fun startPolling() {
        neverStarted = false
        launch {
            kafkaConsumer.use { consumer ->
                consumer.subscribe(listOf(topic))

                while (job.isActive) {
                    pollForAndRelayBatchOfEvents()
                }
            }
        }
    }

    private suspend fun pollForAndRelayBatchOfEvents() = withContext(Dispatchers.IO) {
        try {
            val records = kafkaConsumer.poll(Duration.of(100, ChronoUnit.MILLIS))
            if(records.containsEvents()) {
                eventBatchRelayService.relayEvents(records)
                kafkaConsumer.commitSync()
            }
        } catch (re: RetriableKafkaException) {
            log.warn("Post mot Kafka feilet, prøver igjen senere. Topic: $topic", re)
            rollbackOffset()

        } catch (ure: UnretriableKafkaException) {
            log.warn("Alvorlig feil ved post mot kafka. Stopper polling. Topic: $topic", ure)
            stopPolling()

        } catch (ce: CancellationException) {
            log.info("Denne coroutine-en ble stoppet. ${ce.message}", ce)

        } catch (e: Exception) {
            log.error("Noe uventet feilet, stopper polling. Topic: $topic", e)
            stopPolling()
        }
    }

    fun ConsumerRecords<Nokkel, T>.containsEvents() = count() > 0

    private suspend fun rollbackOffset() {
        withContext(Dispatchers.IO) {
            kafkaConsumer.rollbackToLastCommitted()
        }
    }
}
