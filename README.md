# Kafka Order Processing System (Java + Avro)

This repository contains an implementation of the **Assignment – Chapter 3**:  
A Kafka-based distributed system that **produces and consumes order messages** using **Java**, **Avro serialization**, **retry logic**, **DLQ**, and **real-time aggregation**.

The assignment details come from the provided instructions PDF.  
Each order message contains:  
- `orderId` – string  
- `product` – string  
- `price` – float  

The system must support:  
✔ Real-time aggregation (running average of prices)  
✔ Retry logic for temporary failures  
✔ Dead-Letter Queue for permanently failed messages  
✔ Live demonstration  
✔ Clean Git repository

---

## 📁 Project Structure

```
/src
  /main/java/com/example/kafka
      ProducerApp.java
      ConsumerApp.java
      AverageAggregator.java
      RetryHandler.java
      DlqProducer.java
  /main/resources
      order.avsc
      application.properties
pom.xml
README.md
```

---

## 📦 Avro Schema

Create the file:

`src/main/resources/order.avsc`

```json
{
  "namespace": "com.example.kafka",
  "type": "record",
  "name": "Order",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "product", "type": "string" },
    { "name": "price", "type": "float" }
  ]
}
```

---

follow the steps below **in order** to build the full Kafka system using Java.

---

## 1️⃣ Create a Java + Maven Project 


- SLF4J
- Maven Exec Plugin

---

## 2️⃣ Implement the Kafka **Order Producer**

`ProducerApp.java` must:

- Load the Avro schema
- Generate random orders with:
  - Random `orderId`
  - Random product name
  - Random price
- Serialize using Avro
- Publish to topic: **orders**
- Allow configurable production rate

### Producer Features

- Use `KafkaAvroSerializer`
- Ensure idempotent producer settings
- Logging each message

---

## 3️⃣ Implement the Kafka **Order Consumer**

`ConsumerApp.java` must:

### ✔ Deserialize using Avro  
Use `KafkaAvroDeserializer`.

### ✔ Real-time aggregation  
Use a separate class `AverageAggregator` to:

- Maintain running average of `price` per product
- Print the updated average for each new message

### ✔ Retry Logic  
Create `RetryHandler` with:

- Exponential backoff retry
- Configurable max retries
- Re-processing failed messages in `orders-retry`

### ✔ Dead Letter Queue  
If retries exceed the max limit:

- Send message to topic: **orders-dlq**
- Implement using `DlqProducer`

### ✔ Logging  
Every retry attempt must be logged.

---

## 4️⃣ Create Kafka Topics

Ensure the system uses these topics:

| Purpose                | Topic Name     |
|------------------------|----------------|
| Main order messages    | `orders`       |
| Retry processing       | `orders-retry` |
| Dead Letter Queue (DLQ)| `orders-dlq`   |

---

## 5️⃣ Add Configuration File

Create:

### `src/main/resources/application.properties`

Include:

- Kafka bootstrap server
- Schema registry URL
- Producer configs
- Consumer configs
- Retry settings
- DLQ topic name

---

## 6️⃣ Running the System

### Start Producer
```
mvn exec:java -Dexec.mainClass="com.example.kafka.ProducerApp"
```

### Start Consumer
```
mvn exec:java -Dexec.mainClass="com.example.kafka.ConsumerApp"
```

---

## 7️⃣ Error Handling & Logging

Implement:

- Try/catch around all Kafka operations
- Custom exceptions for retryable vs non-retryable errors
- Logging for:
  - New messages
  - Aggregation updates
  - Retry attempts
  - DLQ handling

---

# ✅ Completion Checklist

- [ ] Producer publishes Avro messages
- [ ] Consumer deserializes Avro messages
- [ ] Running average works continuously
- [ ] Retry system successfully retries failed messages
- [ ] DLQ messages appear in `orders-dlq`
- [ ] Compilation + runtime working with Maven

