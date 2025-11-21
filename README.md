# Kafka Order Processing System (Java + Avro)

Kafka-based producer/consumer demo that publishes order events, retries transient failures, forwards permanently failed messages to a DLQ, and keeps a running average of order prices per product.

## Project Structure
```
src/main/java/com/example/kafka
  ├─ config/        # helpers for loading schemas/properties and building Kafka configs
  ├─ controller/    # ProducerController and ConsumerController entrypoints
  ├─ model/         # Order domain record
  └─ service/       # producer, consumer, retry, aggregation, and DLQ services
src/main/resources  # Avro schema + application.properties
pom.xml             # Maven build
Dockerfile          # container build
```

## Requirements
- Java 17+
- Maven 3.9+
- Kafka broker and (optionally) Confluent Schema Registry reachable from your machine or container

## Local development
1. Install dependencies and build:
   ```bash
   mvn -q -DskipTests package
   ```

2. Start the producer (publishes random orders to the `orders` topic):
   ```bash
   mvn -q exec:java -Dexec.mainClass="com.example.kafka.controller.ProducerController"
   ```

3. Start the consumer (aggregates, retries, DLQ):
   ```bash
   mvn -q exec:java -Dexec.mainClass="com.example.kafka.controller.ConsumerController"
   ```

Configuration defaults live in `src/main/resources/application.properties`. Adjust broker, schema registry, retry, and topic settings there as needed. You can also point the app at an external file via the `APP_CONFIG_PATH` environment variable when starting either process.

## Docker usage
The Docker image builds the app with Maven and runs a chosen entrypoint based on `MAIN_CLASS`. To supply a custom properties file, set `APP_CONFIG_PATH` and mount it into the container.

### Build the image
```bash
docker build -t kafka-order-system .
```

### Run the consumer (default)
```bash
docker run --rm \
  -e MAIN_CLASS=com.example.kafka.controller.ConsumerController \
  kafka-order-system
```

### Run the producer
```bash
docker run --rm \
  -e MAIN_CLASS=com.example.kafka.controller.ProducerController \
  kafka-order-system
```

> The container bundles the default `application.properties`. If you need to provide a different configuration, mount it over the packaged file:
> ```bash
> docker run --rm \
>   -e MAIN_CLASS=com.example.kafka.controller.ConsumerController \
>   -e APP_CONFIG_PATH=/app/application.properties \
>   -v /path/to/custom/application.properties:/app/application.properties:ro \
>   kafka-order-system
> ```

## Kafka topics
The application expects the following topics to exist:
- `orders` (main stream)
- `orders-retry` (temporary failures)
- `orders-dlq` (permanent failures)

## Notes
- Logs provide visibility into produced messages, aggregation updates, retry attempts, and DLQ forwarding.
- Idempotent producer settings are enabled in the Kafka properties to reduce duplicate deliveries.
