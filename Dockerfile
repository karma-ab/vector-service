FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Vector Service"
LABEL version="1.0.0"

WORKDIR /app

COPY target/vector-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 12000

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV VESPA_ENDPOINT=http://localhost:8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:12000/api/v1/health/live || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
