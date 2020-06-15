package no.nav.personbruker.dittnav.topic.restoration



import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.topic.restoration.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.topic.restoration.beskjed.BeskjedEventRelay
import no.nav.personbruker.dittnav.topic.restoration.beskjed.CapturingBeskjedEventProcessor
import no.nav.personbruker.dittnav.topic.restoration.common.BeskjedRecord
import no.nav.personbruker.dittnav.topic.restoration.config.EventType
import no.nav.personbruker.dittnav.topic.restoration.config.Kafka
import no.nav.personbruker.dittnav.topic.restoration.kafka.Consumer
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.topic.restoration.kafka.util.createNokkel
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.topic.restoration.metrics.StubMetricsReporter
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class EndToEndTestIT {

    private val backupTopic = "backupBeskjedTopic"
    private val targetTopic = "targetBeskjedTopic"
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(listOf(backupTopic, targetTopic))
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val adminClient = embeddedEnv.adminClient

    private val events = (1..10).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()

    private val capturedBeskjedRecords = ArrayList<BeskjedRecord>()

    init {
        embeddedEnv.start()
    }

    @AfterAll
    fun tearDown() {
        adminClient?.close()
        embeddedEnv.tearDown()
    }

    @Test
    fun `Kafka instansen i minnet har blitt staret`() {
        embeddedEnv.serverPark.status `should equal` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal lese inn Beskjeds-eventer og skrive de til databasen`() {
        `Produserer noen testeventer`()
        `Les inn alle eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`()

        events.all {
            capturedBeskjedRecords.contains(BeskjedRecord(it.key, it.value))
        }
    }

    fun `Produserer noen testeventer`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, backupTopic, events)
        } shouldEqualTo true
    }

    fun `Les inn alle eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`() {
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(consumerProps)

        val producerProps = Kafka.producerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaProducer = KafkaProducer<Nokkel, Beskjed>(producerProps)
        val producerWrapper = KafkaProducerWrapper(targetTopic, kafkaProducer)

        val eventRelay = BeskjedEventRelay(producerWrapper, metricsProbe)
        val consumer = Consumer(backupTopic, kafkaConsumer, eventRelay)

        kafkaProducer.initTransactions()
        runBlocking {
            consumer.startPolling()

            `Wait until all events have been received by target topic`()

            consumer.stopPolling()
        }
    }

    private fun `Wait until all events have been received by target topic`() {
        val targetConsumerProps = Kafka.consumerProps(testEnvironment, EventType.BESKJED, true)
        val targetKafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(targetConsumerProps)
        val capturingProcessor = CapturingBeskjedEventProcessor()

        val targetConsumer = Consumer(targetTopic, targetKafkaConsumer, capturingProcessor)

        var currentNumberOfRecords = 0

        targetConsumer.startPolling()

        while (currentNumberOfRecords < events.size) {
            runBlocking {
                currentNumberOfRecords = capturingProcessor.getEvents().size
                delay(100)
            }
        }

        runBlocking {
            targetConsumer.stopPolling()
        }

        capturedBeskjedRecords.addAll(capturingProcessor.getEvents())
    }

}
