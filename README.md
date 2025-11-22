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

### Running the Spring Boot application locally
1. Start Kafka + Schema Registry (the bundled compose file is the fastest way):
   ```bash
   docker compose up -d zookeeper kafka schema-registry
   ```

2. Launch the full Spring Boot app (produces **and** consumes in one JVM):
   ```bash
   mvn spring-boot:run \
     -Dspring-boot.run.arguments="--demo.mode=both --logging.file.name=target/spring-boot.log --demo.kafka.bootstrap-servers=localhost:9092 --demo.kafka.schema-registry-url=http://localhost:8081"
   ```

   *Use `--demo.mode=producer` or `--demo.mode=consumer` if you only want one side.*

3. Confirm activity by watching the logs—orders will be produced every second and immediately consumed/aggregated:
   ```bash
   # in another terminal
   tail -f target/spring-boot.log
   ```

To run the packaged JAR instead of the Maven plugin, build once (`mvn -DskipTests package`) and execute:
```bash
java -jar target/kafka-order-system-1.0-SNAPSHOT.jar --demo.mode=both --logging.file.name=target/spring-boot.log
```

## Quick start (Docker)
```bash
# Build the application image once
docker compose build app

# Start the whole stack (Kafka, Schema Registry, and the Spring Boot app in "both" mode)
docker compose up -d

# Watch the application logs
docker compose logs -f app
```

Need custom config? Mount it and set `APP_CONFIG_PATH` or override individual Spring properties with environment variables in `docker-compose.yml`:
```yaml
  app:
    # ...
    environment:
      APP_CONFIG_PATH: /app/application.properties
    volumes:
      - /path/to/application.properties:/app/application.properties:ro
```

### Testing the app with Docker
The Docker image can also run the full Spring Boot entrypoint so you can verify end-to-end behavior without installing Java or
Maven locally.

1. Build the image (if you have not already):
   ```bash
   docker compose build app
   ```

2. Bring everything up together—Kafka, Schema Registry, and the Spring Boot app in "both" mode with the right internal endpoints preconfigured:
   ```bash
   docker compose up -d
   ```

3. Watch the app container to ensure orders are produced and consumed:
   ```bash
   docker compose logs -f app
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
