package com.example.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetryService {
    private static final Logger LOGGER = Logger.getLogger(RetryService.class.getName());

    private final int maxAttempts;
    private final long initialBackoffMs;
    private final String retryTopic;
    private final DlqService dlqService;
    private final KafkaProducer<String, Object> retryProducer;

    public RetryService(int maxAttempts, long initialBackoffMs, String retryTopic,
                        DlqService dlqService, KafkaProducer<String, Object> retryProducer) {
        this.maxAttempts = maxAttempts;
        this.initialBackoffMs = initialBackoffMs;
        this.retryTopic = retryTopic;
        this.dlqService = dlqService;
        this.retryProducer = retryProducer;
    }

    public void handleFailure(ConsumerRecord<String, Object> record, Exception exception) {
        int attempt = currentAttempt(record) + 1;
        if (attempt <= maxAttempts) {
            long backoff = (long) (initialBackoffMs * Math.pow(2, attempt - 1));
            LOGGER.log(Level.WARNING, "Retrying record with key {0} (attempt {1} of {2}), waiting {3} ms", new Object[]{
                    record.key(), attempt, maxAttempts, backoff});
            sleep(backoff);
            ProducerRecord<String, Object> retryRecord = new ProducerRecord<>(retryTopic, record.key(), record.value());
            retryRecord.headers().add(new RecordHeader("retries", String.valueOf(attempt).getBytes(StandardCharsets.UTF_8)));
            retryProducer.send(retryRecord, (metadata, sendEx) -> {
                if (sendEx != null) {
                    LOGGER.severe("Failed to send record to retry topic: " + sendEx.getMessage());
                } else {
                    LOGGER.info(() -> "Sent record to retry topic " + metadata.topic() + " partition "
                            + metadata.partition() + " offset " + metadata.offset());
                }
            });
        } else {
            LOGGER.log(Level.SEVERE, "Exceeded max retries for key {0}. Sending to DLQ", record.key());
            dlqService.send(record);
        }
    }

    private int currentAttempt(ConsumerRecord<String, Object> record) {
        Optional<Header> header = Optional.ofNullable(record.headers().lastHeader("retries"));
        if (header.isEmpty()) {
            return 0;
        }
        try {
            String value = new String(header.get().value(), StandardCharsets.UTF_8);
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            LOGGER.warning("Invalid retries header value");
            return 0;
        }
    }

    private void sleep(long durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
