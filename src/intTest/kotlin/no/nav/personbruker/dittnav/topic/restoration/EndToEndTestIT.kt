package no.nav.personbruker.dittnav.topic.restoration



import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Done
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.topic.restoration.beskjed.AvroBeskjedObjectMother
import no.nav.personbruker.dittnav.topic.restoration.beskjed.BeskjedEventRelay
import no.nav.personbruker.dittnav.topic.restoration.common.RecordKeyValueWrapper
import no.nav.personbruker.dittnav.topic.restoration.config.EventType
import no.nav.personbruker.dittnav.topic.restoration.config.Kafka
import no.nav.personbruker.dittnav.topic.restoration.done.AvroDoneObjectMother
import no.nav.personbruker.dittnav.topic.restoration.done.DoneEventRelay
import no.nav.personbruker.dittnav.topic.restoration.kafka.CapturingEventProcessor
import no.nav.personbruker.dittnav.topic.restoration.kafka.Consumer
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.kafka.util.KafkaTestUtil
import no.nav.personbruker.dittnav.topic.restoration.kafka.util.createNokkel
import no.nav.personbruker.dittnav.topic.restoration.metrics.EventMetricsProbe
import no.nav.personbruker.dittnav.topic.restoration.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.topic.restoration.oppgave.AvroOppgaveObjectMother
import no.nav.personbruker.dittnav.topic.restoration.oppgave.OppgaveEventRelay
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class EndToEndTestIT {

    private val backupBeskjedTopic = "backupBeskjedTopic"
    private val targetBeskjedTopic = "targetBeskjedTopic"
    private val backupOppgaveTopic = "backupOppgaveTopic"
    private val targetOppgaveTopic = "targetOppgaveTopic"
    private val backupDoneTopic = "backupDoneTopic"
    private val targetDoneTopic = "targetDoneTopic"

    private val topicList = listOf(
        backupBeskjedTopic, targetBeskjedTopic,
        backupOppgaveTopic, targetOppgaveTopic,
        backupDoneTopic, targetDoneTopic
    )
    private val embeddedEnv = KafkaTestUtil.createDefaultKafkaEmbeddedInstance(topicList)
    private val testEnvironment = KafkaTestUtil.createEnvironmentForEmbeddedKafka(embeddedEnv)

    private val metricsReporter = StubMetricsReporter()
    private val metricsProbe = EventMetricsProbe(metricsReporter)

    private val adminClient = embeddedEnv.adminClient

    private val beskjedEvents = (1..10).map { createNokkel(it) to AvroBeskjedObjectMother.createBeskjed(it) }.toMap()
    private val oppgaveEvents = (1..10).map { createNokkel(it) to AvroOppgaveObjectMother.createOppgave(it) }.toMap()
    private val doneEvents = (1..10).map { createNokkel(it) to AvroDoneObjectMother.createDone(it) }.toMap()

    private val capturedBeskjedRecords = ArrayList<RecordKeyValueWrapper<Beskjed>>()
    private val capturedOppgaveRecords = ArrayList<RecordKeyValueWrapper<Oppgave>>()
    private val capturedDoneRecords = ArrayList<RecordKeyValueWrapper<Done>>()

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
        embeddedEnv.serverPark.status `should be equal to` KafkaEnvironment.ServerParkStatus.Started
    }

    @Test
    fun `Skal lese inn Beskjed-eventer og sende dem til vanlig topic`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, backupBeskjedTopic, beskjedEvents)
        } shouldBeEqualTo true

        `Les inn alle beskjed eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`()

        beskjedEvents.all {
            capturedBeskjedRecords.contains(RecordKeyValueWrapper(it.key, it.value))
        }
    }

    @Test
    fun `Skal lese inn Oppgave-eventer og sende dem til vanlig topic`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, backupOppgaveTopic, oppgaveEvents)
        } shouldBeEqualTo true

        `Les inn alle oppgave eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`()

        oppgaveEvents.all {
            capturedOppgaveRecords.contains(RecordKeyValueWrapper(it.key, it.value))
        }
    }

    @Test
    fun `Skal lese inn Done-eventer og sende dem til vanlig topic`() {
        runBlocking {
            KafkaTestUtil.produceEvents(testEnvironment, backupDoneTopic, doneEvents)
        } shouldBeEqualTo true

        `Les inn alle done eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`()

        doneEvents.all {
            capturedDoneRecords.contains(RecordKeyValueWrapper(it.key, it.value))
        }
    }


    fun `Les inn alle beskjed eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`() {
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(consumerProps)

        val producerProps = Kafka.producerProps(testEnvironment, EventType.BESKJED, true)
        val kafkaProducer = KafkaProducer<Nokkel, Beskjed>(producerProps)
        val producerWrapper = KafkaProducerWrapper(targetBeskjedTopic, kafkaProducer)

        val eventRelay = BeskjedEventRelay(producerWrapper, metricsProbe)
        val consumer = Consumer(backupBeskjedTopic, kafkaConsumer, eventRelay)

        kafkaProducer.initTransactions()
        runBlocking {
            consumer.startPolling()

            `Wait until all beskjed events have been received by target topic`()

            consumer.stopPolling()
        }
    }

    private fun `Wait until all beskjed events have been received by target topic`() {
        val targetConsumerProps = Kafka.consumerProps(testEnvironment, EventType.BESKJED, true)
        val targetKafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(targetConsumerProps)
        val capturingProcessor = CapturingEventProcessor<Beskjed>()

        val targetConsumer = Consumer(targetBeskjedTopic, targetKafkaConsumer, capturingProcessor)

        var currentNumberOfRecords = 0

        targetConsumer.startPolling()

        while (currentNumberOfRecords < beskjedEvents.size) {
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

    fun `Les inn alle oppgave eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`() {
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.OPPGAVE, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Oppgave>(consumerProps)

        val producerProps = Kafka.producerProps(testEnvironment, EventType.OPPGAVE, true)
        val kafkaProducer = KafkaProducer<Nokkel, Oppgave>(producerProps)
        val producerWrapper = KafkaProducerWrapper(targetOppgaveTopic, kafkaProducer)

        val eventRelay = OppgaveEventRelay(producerWrapper, metricsProbe)
        val consumer = Consumer(backupOppgaveTopic, kafkaConsumer, eventRelay)

        kafkaProducer.initTransactions()
        runBlocking {
            consumer.startPolling()

            `Wait until all oppgave events have been received by target topic`()

            consumer.stopPolling()
        }
    }

    private fun `Wait until all oppgave events have been received by target topic`() {
        val targetConsumerProps = Kafka.consumerProps(testEnvironment, EventType.OPPGAVE, true)
        val targetKafkaConsumer = KafkaConsumer<Nokkel, Oppgave>(targetConsumerProps)
        val capturingProcessor = CapturingEventProcessor<Oppgave>()

        val targetConsumer = Consumer(targetOppgaveTopic, targetKafkaConsumer, capturingProcessor)

        var currentNumberOfRecords = 0

        targetConsumer.startPolling()

        while (currentNumberOfRecords < oppgaveEvents.size) {
            runBlocking {
                currentNumberOfRecords = capturingProcessor.getEvents().size
                delay(100)
            }
        }

        runBlocking {
            targetConsumer.stopPolling()
        }

        capturedOppgaveRecords.addAll(capturingProcessor.getEvents())
    }

    fun `Les inn alle done eventene fra backup og verifiser at de har blitt lagt til i vanlig topic`() {
        val consumerProps = Kafka.consumerProps(testEnvironment, EventType.DONE, true)
        val kafkaConsumer = KafkaConsumer<Nokkel, Done>(consumerProps)

        val producerProps = Kafka.producerProps(testEnvironment, EventType.DONE, true)
        val kafkaProducer = KafkaProducer<Nokkel, Done>(producerProps)
        val producerWrapper = KafkaProducerWrapper(targetDoneTopic, kafkaProducer)

        val eventRelay = DoneEventRelay(producerWrapper, metricsProbe)
        val consumer = Consumer(backupDoneTopic, kafkaConsumer, eventRelay)

        kafkaProducer.initTransactions()
        runBlocking {
            consumer.startPolling()

            `Wait until all done events have been received by target topic`()

            consumer.stopPolling()
        }
    }

    private fun `Wait until all done events have been received by target topic`() {
        val targetConsumerProps = Kafka.consumerProps(testEnvironment, EventType.DONE, true)
        val targetKafkaConsumer = KafkaConsumer<Nokkel, Done>(targetConsumerProps)
        val capturingProcessor = CapturingEventProcessor<Done>()

        val targetConsumer = Consumer(targetDoneTopic, targetKafkaConsumer, capturingProcessor)

        var currentNumberOfRecords = 0

        targetConsumer.startPolling()

        while (currentNumberOfRecords < doneEvents.size) {
            runBlocking {
                currentNumberOfRecords = capturingProcessor.getEvents().size
                delay(100)
            }
        }

        runBlocking {
            targetConsumer.stopPolling()
        }

        capturedDoneRecords.addAll(capturingProcessor.getEvents())
    }

}
