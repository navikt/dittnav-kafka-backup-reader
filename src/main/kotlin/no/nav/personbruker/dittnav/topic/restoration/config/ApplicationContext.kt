package no.nav.personbruker.dittnav.topic.restoration.config

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.topic.restoration.beskjed.BeskjedEventRelay
import no.nav.personbruker.dittnav.topic.restoration.done.DoneEventRelay
import no.nav.personbruker.dittnav.topic.restoration.health.HealthService
import no.nav.personbruker.dittnav.topic.restoration.innboks.InnboksEventRelay
import no.nav.personbruker.dittnav.topic.restoration.kafka.Consumer
import no.nav.personbruker.dittnav.topic.restoration.kafka.KafkaProducerWrapper
import no.nav.personbruker.dittnav.topic.restoration.metrics.buildEventMetricsProbe
import no.nav.personbruker.dittnav.topic.restoration.oppgave.OppgaveEventRelay
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory

class ApplicationContext {

    private val log = LoggerFactory.getLogger(ApplicationContext::class.java)
    val environment = Environment()

    val eventMetricsProbe = buildEventMetricsProbe(environment)


    val beskjedKafkaProps = Kafka.consumerProps(environment, EventType.BESKJED)
    var beskjedConsumer = initializeBeskjedConsumer()

    val oppgaveKafkaProps = Kafka.consumerProps(environment, EventType.OPPGAVE)
    var oppgaveConsumer = initializeOppgaveConsumer()

    val innboksKafkaProps = Kafka.consumerProps(environment, EventType.INNBOKS)
    var innboksConsumer = initializeInnboksConsumer()

    val doneKafkaProps = Kafka.consumerProps(environment, EventType.DONE)
    var doneConsumer = initializeDoneConsumer()

    val healthService = HealthService(this)

    private fun initializeBeskjedConsumer(): Consumer<Beskjed> {
        val beskjedKafkaProducer = KafkaProducer<Nokkel, Beskjed>(Kafka.producerProps(environment, EventType.BESKJED))
        beskjedKafkaProducer.initTransactions()

        val beskjedKafkaProducerWrapper = KafkaProducerWrapper(Kafka.beskjedTopicName, beskjedKafkaProducer)
        val beskjedEventRelay = BeskjedEventRelay(beskjedKafkaProducerWrapper, eventMetricsProbe)
        return KafkaConsumerSetup.setupConsumerForTheBeskjedTopic(beskjedKafkaProps, beskjedEventRelay)
    }
    
    private fun initializeOppgaveConsumer(): Consumer<Oppgave> {
        val oppgaveKafkaProducer = KafkaProducer<Nokkel, Oppgave>(Kafka.producerProps(environment, EventType.OPPGAVE))
        oppgaveKafkaProducer.initTransactions()

        val oppgaveKafkaProducerWrapper = KafkaProducerWrapper(Kafka.oppgaveTopicName, oppgaveKafkaProducer)
        val oppgaveEventRelay = OppgaveEventRelay(oppgaveKafkaProducerWrapper, eventMetricsProbe)
        return KafkaConsumerSetup.setupConsumerForTheOppgaveTopic(oppgaveKafkaProps, oppgaveEventRelay)
    }    
    
    private fun initializeInnboksConsumer(): Consumer<Innboks> {
        val innboksKafkaProducer = KafkaProducer<Nokkel, Innboks>(Kafka.producerProps(environment, EventType.INNBOKS))
        innboksKafkaProducer.initTransactions()

        val innboksKafkaProducerWrapper = KafkaProducerWrapper(Kafka.innboksTopicName, innboksKafkaProducer)
        val innboksEventRelay = InnboksEventRelay(innboksKafkaProducerWrapper, eventMetricsProbe)
        return KafkaConsumerSetup.setupConsumerForTheInnboksTopic(innboksKafkaProps, innboksEventRelay)
    }    
    
    private fun initializeDoneConsumer(): Consumer<Done> {
        val doneKafkaProducer = KafkaProducer<Nokkel, Done>(Kafka.producerProps(environment, EventType.DONE))
        doneKafkaProducer.initTransactions()

        val doneKafkaProducerWrapper = KafkaProducerWrapper(Kafka.doneTopicName, doneKafkaProducer)
        val doneEventRelay = DoneEventRelay(doneKafkaProducerWrapper, eventMetricsProbe)
        return KafkaConsumerSetup.setupConsumerForTheDoneTopic(doneKafkaProps, doneEventRelay)
    }


    fun reinitializeConsumers() {
        if (beskjedConsumer.isCompleted()) {
            beskjedConsumer = initializeBeskjedConsumer()
            log.info("beskjedConsumer har blitt reinstansiert.")
        } else {
            log.warn("beskjedConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (oppgaveConsumer.isCompleted()) {
            oppgaveConsumer = initializeOppgaveConsumer()
            log.info("oppgaveConsumer har blitt reinstansiert.")
        } else {
            log.warn("oppgaveConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (innboksConsumer.isCompleted()) {
            innboksConsumer = initializeInnboksConsumer()
            log.info("innboksConsumer har blitt reinstansiert.")
        } else {
            log.warn("innboksConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }

        if (doneConsumer.isCompleted()) {
            doneConsumer = initializeDoneConsumer()
            log.info("doneConsumer har blitt reinstansiert.")
        } else {
            log.warn("doneConsumer kunne ikke bli reinstansiert fordi den fortsatt er aktiv.")
        }
    }
}
