package com.assignment.kafka.consumer;

import com.assignment.kafka.avro.Order;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class DLQConsumer {

    private static final String DLQ_TOPIC = "orders-dlq";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:19092");
        props.put("group.id", "dlq-consumer-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", "true");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(DLQ_TOPIC));

        System.out.println("DLQ consumer started.");

        while (true) {
            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, Order> rec : records) {
                System.err.println("DLQ message received, manual investigation required: " + rec.value());
            }
        }
    }
}
