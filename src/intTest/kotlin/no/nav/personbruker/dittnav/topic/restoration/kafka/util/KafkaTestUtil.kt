package no.nav.personbruker.dittnav.topic.restoration.kafka.util

import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import no.nav.personbruker.dittnav.topic.restoration.config.Environment
import org.apache.avro.generic.GenericRecord
import java.util.*

object KafkaTestUtil {

    val username = "srvkafkaclient"
    val password = "kafkaclient"

    fun createDefaultKafkaEmbeddedInstance(topics: List<String>): KafkaEnvironment {
        return KafkaEnvironment(
                topicNames = topics,
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password)),
                brokerConfigOverrides = Properties().apply {
                    put("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                    put("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                }
        )
    }

    fun createKafkaEmbeddedInstanceWithNumPartitions(topics: List<String>, partitions: Int): KafkaEnvironment {
        val topicInfos = topics.map { KafkaEnvironment.TopicInfo(it, partitions = partitions) }

        return KafkaEnvironment(
                topicInfos = topicInfos,
                withSecurity = true,
                withSchemaRegistry = true,
                users = listOf(JAASCredential(username, password))
        )
    }

    fun createEnvironmentForEmbeddedKafka(embeddedEnv: KafkaEnvironment): Environment {
        return Environment(
                bootstrapServers = embeddedEnv.brokersURL.substringAfterLast("/"),
                schemaRegistryUrl = embeddedEnv.schemaRegistry!!.url,
                username = username,
                password = password,
                groupId = "groupId-for-tests",
                clusterName = "clusterNameIkkeIBrukHer",
                namespace = "namespaceIkkeIBrukHer",
                sensuHost = "sensuHostIkkeIBrukHer",
                sensuPort = "sensuPortIkkeIBrukHer"
        )
    }

    suspend fun produceEvents(env: Environment, topicName: String, events: Map<Nokkel, GenericRecord>): Boolean {
        return KafkaProducerUtil.kafkaAvroProduce(
                env.bootstrapServers,
                env.schemaRegistryUrl,
                topicName,
                env.username,
                env.password,
                events)
    }

}
