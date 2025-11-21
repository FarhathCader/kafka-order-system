package com.assignment.kafka.consumer;

import com.assignment.kafka.avro.Order;
import com.assignment.kafka.util.ProducerUtil;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OrderConsumer {

    private static final String MAIN_TOPIC = "orders";
    private static final String RETRY_TOPIC = "orders-retry";
    private static final String DLQ_TOPIC = "orders-dlq";

    private static float sum = 0;
    private static int count = 0;

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:19092");
        props.put("group.id", "order-consumer-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", "true");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        KafkaProducer<String, Order> producer = ProducerUtil.buildProducer();

        consumer.subscribe(Collections.singletonList(MAIN_TOPIC));

        System.out.println("Main consumer started.");

        while (true) {
            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, Order> rec : records) {
                try {
                    Order order = rec.value();

                    // Example of a temporary failure condition:
                    if (order.getPrice() < 10.0f) {
                        throw new RuntimeException("Price too low, failing message for demo.");
                    }

                    sum += order.getPrice();
                    count++;
                    float avg = sum / count;

                    System.out.printf("Processed: %s | Running Avg Price = %.2f%n", order, avg);

                } catch (Exception e) {
                    System.err.println("Error processing message, sending to retry/DLQ: " + e.getMessage());
                    handleFailure(rec, producer);
                }
            }
        }
    }

    private static void handleFailure(ConsumerRecord<String, Order> rec, KafkaProducer<String, Order> producer) {
        int retryCount = 0;
        Header header = rec.headers().lastHeader("retry-count");
        if (header != null) {
            retryCount = Integer.parseInt(new String(header.value(), StandardCharsets.UTF_8));
        }

        if (retryCount < 3) {
            ProducerRecord<String, Order> retryRecord =
                    new ProducerRecord<>(RETRY_TOPIC, rec.key(), rec.value());
            retryRecord.headers().add("retry-count",
                    String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8));
            producer.send(retryRecord);
            System.out.printf("Sent to retry topic (%d): %s%n", retryCount + 1, rec.value());
        } else {
            ProducerRecord<String, Order> dlqRecord =
                    new ProducerRecord<>(DLQ_TOPIC, rec.key(), rec.value());
            producer.send(dlqRecord);
            System.out.printf("Sent to DLQ after %d retries: %s%n", retryCount, rec.value());
        }
    }
}
