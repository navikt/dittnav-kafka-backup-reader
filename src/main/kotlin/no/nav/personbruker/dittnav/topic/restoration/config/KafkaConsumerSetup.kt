package no.nav.personbruker.dittnav.topic.restoration.config

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchProcessorService
import no.nav.personbruker.dittnav.topic.restoration.kafka.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object KafkaConsumerSetup {

    private val log: Logger = LoggerFactory.getLogger(KafkaConsumerSetup::class.java)

    fun startAllKafkaPollers(appContext: ApplicationContext) {
        appContext.beskjedConsumer.startPolling()
        appContext.oppgaveConsumer.startPolling()
        appContext.doneConsumer.startPolling()
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")
        appContext.beskjedConsumer.stopPolling()
        appContext.oppgaveConsumer.stopPolling()
        appContext.doneConsumer.stopPolling()
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Beskjed>): Consumer<Beskjed> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(kafkaProps)
        return Consumer(Kafka.beskjedBackupTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Oppgave>): Consumer<Oppgave> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveBackupTopicName, kafkaConsumer, eventProcessor)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventProcessor: EventBatchProcessorService<Done>): Consumer<Done> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Done>(kafkaProps)
        return Consumer(Kafka.doneBackupTopicName, kafkaConsumer, eventProcessor)
    }
}
