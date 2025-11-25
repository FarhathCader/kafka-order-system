# Kafka Order System

A Java 17 Kafka demo that produces order events, consumes them with retry + DLQ handling, and computes running averages with Kafka Streams. The project uses Avro for message schemas and ships with Docker tooling for a local Confluent Platform stack.

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker and Docker Compose (for running Kafka, ZooKeeper, and Schema Registry)

## Configuration
Application settings live in [`src/main/resources/application.properties`](src/main/resources/application.properties). Update broker URLs, Schema Registry credentials, topic names, and retry settings as needed.

## Running with Docker Compose (recommended)
This repository includes a Compose file that starts ZooKeeper, Kafka, Schema Registry, and the application container. The application image runs whichever main class you set via the `MAIN_CLASS` environment variable (defaults to the consumer).

```bash
# Build the app image and start the stack
docker-compose up --build

# Override the entrypoint to run the producer instead
MAIN_CLASS=com.example.kafka.ProducerMain docker-compose up --build app
```

Services exposed locally:
- Kafka broker: `localhost:9092`
- Schema Registry: `http://localhost:8081`
- App container: `localhost:8080` (port reserved; the sample app is CLI-driven)

## Running locally without Docker
1. Build the project and copy runtime dependencies:
   ```bash
   mvn -DskipTests package dependency:copy-dependencies
   ```
2. Run either the producer or consumer directly with the classpath that includes the shaded JAR and copied dependencies.

   **macOS/Linux (colon-separated classpath):**
   ```bash
   # Producer
   java -cp target/kafka-order-system-1.0-SNAPSHOT.jar:target/dependency/* \
        com.example.kafka.ProducerMain

   # Consumer (includes retry + DLQ handling)
   java -cp target/kafka-order-system-1.0-SNAPSHOT.jar:target/dependency/* \
        com.example.kafka.ConsumerMain
   ```

   **Windows PowerShell (semicolon-separated classpath):**
   ```powershell
   # Producer
   java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" `
        com.example.kafka.ProducerMain

   # Consumer (includes retry + DLQ handling)
   java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" `
        com.example.kafka.ConsumerMain
   ```

## Useful artifacts
- Avro schema: [`src/main/resources/order.avsc`](src/main/resources/order.avsc)
- Docker build definition: [`Dockerfile`](Dockerfile)
- Local stack definition: [`docker-compose.yml`](docker-compose.yml)
