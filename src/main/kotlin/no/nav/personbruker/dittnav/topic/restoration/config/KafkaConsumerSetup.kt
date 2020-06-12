package no.nav.personbruker.dittnav.topic.restoration.config

import no.nav.brukernotifikasjon.schemas.*
import no.nav.personbruker.dittnav.topic.restoration.common.EventBatchRelayService
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
        if (isOtherEnvironmentThanProd()) {
            appContext.innboksConsumer.startPolling()
        } else {
            log.info("Er i produksjonsmiljø, unnlater å starte innboksconsumer.")
        }
    }

    suspend fun stopAllKafkaConsumers(appContext: ApplicationContext) {
        log.info("Begynner å stoppe kafka-pollerne...")
        appContext.beskjedConsumer.stopPolling()
        appContext.oppgaveConsumer.stopPolling()
        appContext.doneConsumer.stopPolling()
        if (isOtherEnvironmentThanProd()) {
            appContext.innboksConsumer.stopPolling()
        }
        log.info("...ferdig med å stoppe kafka-pollerne.")
    }

    fun setupConsumerForTheBeskjedTopic(kafkaProps: Properties, eventRelay: EventBatchRelayService<Beskjed>): Consumer<Beskjed> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Beskjed>(kafkaProps)
        return Consumer(Kafka.beskjedBackupTopicName, kafkaConsumer, eventRelay)
    }

    fun setupConsumerForTheOppgaveTopic(kafkaProps: Properties, eventRelay: EventBatchRelayService<Oppgave>): Consumer<Oppgave> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Oppgave>(kafkaProps)
        return Consumer(Kafka.oppgaveBackupTopicName, kafkaConsumer, eventRelay)
    }

    fun setupConsumerForTheInnboksTopic(kafkaProps: Properties, eventRelay: EventBatchRelayService<Innboks>): Consumer<Innboks> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Innboks>(kafkaProps)
        return Consumer(Kafka.innboksBackupTopicName, kafkaConsumer, eventRelay)
    }

    fun setupConsumerForTheDoneTopic(kafkaProps: Properties, eventRelay: EventBatchRelayService<Done>): Consumer<Done> {
        val kafkaConsumer = KafkaConsumer<Nokkel, Done>(kafkaProps)
        return Consumer(Kafka.doneBackupTopicName, kafkaConsumer, eventRelay)
    }
}
