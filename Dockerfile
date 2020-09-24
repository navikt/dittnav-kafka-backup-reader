FROM navikt/java:13-appdynamics
COPY init.sh /init-scripts/init.sh
COPY build/libs/dittnav-kafka-backup-reader-all.jar /app/app.jar

ENV PORT=8080
EXPOSE $PORT
