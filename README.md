# Kafka Order Processing System

Simple producer/consumer demo that sends order events to Kafka, retries transient failures, and moves permanent ones to a DLQ.

## Quick start (local)
Needs Java 17+, Maven 3.9+, Kafka, **and** a reachable Schema Registry (the app always uses Avro serializers). Default settings live in `src/main/resources/application.properties`.

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

## Troubleshooting `AbstractKafkaSchemaSerDe` errors
If the application fails with `java.lang.ClassNotFoundException: io.confluent.kafka.serializers.AbstractKafkaSchemaSerDe`, make sure
the Confluent serializer artifacts are on the classpath. The project now declares the needed Confluent repository and explicitly
depends on `io.confluent:kafka-schema-serializer`, so a fresh `mvn clean package` should pull the correct JARs. If you still see the
error:

- Verify the Confluent dependencies resolved successfully (`mvn dependency:tree | grep confluent`).
- Check that your IDE or runtime includes both the built `app.jar` **and** the `lib/` directory copied by `dependency:copy-dependencies`.
- Align versions: the Confluent dependency version is managed via the `confluent.version` property in `pom.xml`.

## Kafka topics
- `orders` (main)
- `orders-retry` (temporary failures)
- `orders-dlq` (permanent failures)
