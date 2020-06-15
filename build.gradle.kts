import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val prometheusVersion = "0.6.0"
val ktorVersion = "1.3.0"
val junitVersion = "5.4.1"
val kafkaVersion = "2.2.0"
val confluentVersion = "5.2.0"
val brukernotifikasjonSchemaVersion = "1.2020.02.07-13.16-fa9d319688b1"
val logstashVersion = 5.2
val logbackVersion = "1.2.3"
val vaultJdbcVersion = "1.3.1"
val jacksonVersion = "2.9.9"
val kluentVersion = "1.52"
val kafkaEmbeddedEnvVersion = "2.1.1"
val mockkVersion = "1.9.3"
val influxdbVersion = "2.8"


plugins {
    val kotlinVersion = "1.3.61"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion

    application
}

repositories {
    mavenCentral()
    maven("http://packages.confluent.io/maven")
    jcenter()
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}
sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["intTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("no.nav:vault-jdbc:$vaultJdbcVersion")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile("io.prometheus:simpleclient_logback:$prometheusVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("org.apache.kafka:kafka-clients:$kafkaVersion")
    compile("io.confluent:kafka-avro-serializer:$confluentVersion")
    compile("no.nav:brukernotifikasjon-schemas:$brukernotifikasjonSchemaVersion")
    compile("org.influxdb:influxdb-java:$influxdbVersion")
    compile("io.ktor:ktor-html-builder:$ktorVersion")
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testCompile(kotlin("test-junit5"))
    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedEnvVersion")
    testImplementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    testImplementation("org.apache.kafka:kafka-streams:$kafkaVersion")
    testImplementation("io.confluent:kafka-schema-registry:$confluentVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    intTestImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = application.mainClassName
        }
        from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {

        environment("NAIS_CLUSTER_NAME", "dev-sbs")
        environment("NAIS_NAMESPACE", "q1")
        environment("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092")
        environment("KAFKA_SCHEMAREGISTRY_SERVERS", "http://localhost:8081")
        environment("SERVICEUSER_USERNAME", "username")
        environment("SERVICEUSER_PASSWORD", "password")
        environment("GROUP_ID", "dittnav_events_backup_reader")
        environment("SENSU_HOST", "stub")
        environment("SENSU_PORT", "")

        main = application.mainClassName
        classpath = sourceSets["main"].runtimeClasspath
    }
}


val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }