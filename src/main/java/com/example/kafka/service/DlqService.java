package com.example.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import java.util.logging.Logger;

public class DlqService {
    private static final Logger LOGGER = Logger.getLogger(DlqService.class.getName());

    private final KafkaProducer<String, Object> producer;
    private final String dlqTopic;

    public DlqService(KafkaProducer<String, Object> producer, String dlqTopic) {
        this.producer = producer;
        this.dlqTopic = dlqTopic;
    }

    public void send(ConsumerRecord<String, Object> record) {
        ProducerRecord<String, Object> dlqRecord = new ProducerRecord<>(dlqTopic, record.key(), record.value());
        for (Header header : record.headers()) {
            dlqRecord.headers().add(header.key(), header.value());
        }
        producer.send(dlqRecord, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.severe("Failed to send record to DLQ: " + exception.getMessage());
            } else {
                LOGGER.info(() -> "Sent to DLQ offset " + metadata.offset() + " (key " + record.key() + ")");
            }
        });
    }
}
