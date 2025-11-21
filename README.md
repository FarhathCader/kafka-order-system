# Kafka Order Processing System

Simple producer/consumer demo that sends order events to Kafka, retries transient failures, and moves permanent ones to a DLQ.

## Quick start (local)
Needs Java 17+, Maven 3.9+, and access to Kafka (plus Schema Registry if you use Avro). Default settings live in `src/main/resources/application.properties`.

```bash
# build
mvn -q -DskipTests package

# run producer (writes random orders to "orders")
mvn -q exec:java -Dexec.mainClass="com.example.kafka.controller.ProducerController"

# run consumer (aggregates, retries, DLQ)
mvn -q exec:java -Dexec.mainClass="com.example.kafka.controller.ConsumerController"
```

Point to a different config with `APP_CONFIG_PATH=/path/to/application.properties` when launching either process.

## Quick start (Docker)
```bash
# build image
docker build -t kafka-order-system .

# consumer (default)
docker run --rm -e MAIN_CLASS=com.example.kafka.controller.ConsumerController kafka-order-system

# producer
docker run --rm -e MAIN_CLASS=com.example.kafka.controller.ProducerController kafka-order-system
```

Need custom config? Mount it and set `APP_CONFIG_PATH`:
```bash
docker run --rm \
  -e MAIN_CLASS=com.example.kafka.controller.ConsumerController \
  -e APP_CONFIG_PATH=/app/application.properties \
  -v /path/to/application.properties:/app/application.properties:ro \
  kafka-order-system
```

## Kafka topics
- `orders` (main)
- `orders-retry` (temporary failures)
- `orders-dlq` (permanent failures)
