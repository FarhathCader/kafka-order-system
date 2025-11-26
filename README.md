# Kafka Order Processing System (Java + Avro + Docker)

This project implements a Kafka-based order processing system with:

- Java producer and consumer
- Avro serialization
- Real-time running average of prices
- Retry topic and Dead Letter Queue (DLQ)
- Kafka, Zookeeper and Schema Registry running in Docker

## Prerequisites

- JDK 17
- Maven
- Docker & Docker Compose

## 1. Start Kafka stack in Docker

```bash
docker-compose up -d
docker ps
```

## 2. Generate Avro classes and build

```bash
mvn clean package
```

## 3. Run the consumers (in separate terminals / Run Configurations)

```bash
# main consumer
java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" com.assignment.kafka.consumer.OrderConsumer

# retry consumer
java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" com.assignment.kafka.consumer.RetryConsumer"

# DLQ consumer (optional)
java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" com.assignment.kafka.consumer.DLQConsumer"
```

## 4. Run the producer

```bash
java -cp "target/kafka-order-system-1.0-SNAPSHOT.jar;target/dependency/*" com.assignment.kafka.producer.OrderProducer"
```

The main consumer prints processed orders and a running average of price.
If processing fails, messages go to the retry topic and eventually to the DLQ after 3 failed attempts.
