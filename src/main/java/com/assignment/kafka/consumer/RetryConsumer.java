package com.assignment.kafka.consumer;

import com.assignment.kafka.avro.Order;
import com.assignment.kafka.util.ProducerUtil;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class RetryConsumer {

    private static final String RETRY_TOPIC = "orders-retry";
    private static final String MAIN_TOPIC = "orders";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:19092");
        props.put("group.id", "retry-consumer-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", "true");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, Order> consumer = new KafkaConsumer<>(props);
        KafkaProducer<String, Order> producer = ProducerUtil.buildProducer();

        consumer.subscribe(Collections.singletonList(RETRY_TOPIC));

        System.out.println("Retry consumer started.");

        while (true) {
            ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, Order> rec : records) {
                ProducerRecord<String, Order> resend =
                        new ProducerRecord<>(MAIN_TOPIC, rec.key(), rec.value());

                // preserve headers (including retry-count)
                for (Header h : rec.headers()) {
                    resend.headers().add(h);
                }

                producer.send(resend);
                System.out.println("Retrying message back to main topic: " + rec.value());
            }
        }
    }
}
