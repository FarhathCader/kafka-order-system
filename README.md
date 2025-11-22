# Kafka Order Processing System

## Run the Spring Boot application locally
1. Ensure Java 17+ and Maven 3.9+ are installed.
2. Start the dependencies (Kafka, ZooKeeper, Schema Registry):
   ```bash
   docker compose up -d zookeeper kafka schema-registry
   ```
3. Launch the Spring Boot app (producing and consuming in one JVM):
   ```bash
   mvn spring-boot:run \
     -Dspring-boot.run.arguments="--demo.mode=both --demo.kafka.bootstrap-servers=localhost:9092 --demo.kafka.schema-registry-url=http://localhost:8081"
   ```
4. Watch the logs to confirm orders are being produced and consumed:
   ```bash
   tail -f target/spring-boot.log
   ```

## Run the application with Docker Compose
1. Build the application image:
   ```bash
   docker compose build app
   ```
2. Start the full stack (Kafka, Schema Registry, and the Spring Boot app):
   ```bash
   docker compose up -d
   ```
3. View application logs:
   ```bash
   docker compose logs -f app
   ```
