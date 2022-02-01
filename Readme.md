# DittNAV Kafka backup reader

Denne appen leser inn brukernotifikasjoner fra backup-topics og inn igjen på vanlige topics.

# Kom i gang
1. Bygge dittnav-kafka-backup-reader:
    * bygge og kjøre integrasjonstester: `gradle clean build`
2. Start appen ved å kjøre kommandoen `gradle runServer`
3. Polling mot kafka må startes manuelt med et kall mot /internal/polling/start
