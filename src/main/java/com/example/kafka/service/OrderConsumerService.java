package com.example.kafka.service;

import com.example.kafka.model.Order;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderConsumerService {
    private static final Logger LOGGER = Logger.getLogger(OrderConsumerService.class.getName());

    private final KafkaConsumer<String, Object> consumer;
    private final RetryService retryService;
    private final AverageAggregator averageAggregator;
    private final String ordersTopic;
    private final String retryTopic;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
    public OrderConsumerService(KafkaConsumer<String, Object> consumer,
                                RetryService retryService,
                                AverageAggregator averageAggregator,
                                String ordersTopic,
                                String retryTopic) {
        this.consumer = consumer;
        this.retryService = retryService;
        this.averageAggregator = averageAggregator;
        this.ordersTopic = ordersTopic;
        this.retryTopic = retryTopic;
    }

    public void start() {
        consumer.subscribe(Arrays.asList(ordersTopic, retryTopic));
        LOGGER.info(() -> "Consumer started. Subscribed to " + ordersTopic + " and " + retryTopic);
        try {
            while (running.get()) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, Object> record : records) {
                    try {
                        processRecord(record);
                        consumer.commitAsync();
                    } catch (Exception ex) {
                        retryService.handleFailure(record, ex);
                    }
                }
            }
        } catch (WakeupException e) {
            if (running.get()) {
                throw e;
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }

    private void processRecord(ConsumerRecord<String, Object> record) {
        Object value = record.value();
        if (!(value instanceof GenericRecord genericRecord)) {
            throw new IllegalArgumentException("Unexpected record type: " + value);
        }
        Order order = Order.fromGenericRecord(genericRecord);
        enforceDemoFailures(order);
        double average = averageAggregator.updateAverage(order.getProduct(), order.getPrice());
        LOGGER.log(Level.INFO, "Consumed key {0} product {1} price {2} -> running average {3}",
                new Object[]{record.key(), order.getProduct(), order.getPrice(), average});
    }
    private void enforceDemoFailures(Order order) {
        if ("temporary-failure".equalsIgnoreCase(order.getProduct())) {
            int attempt = failureCounts.merge(order.getOrderId(), 1, Integer::sum);
            if (attempt == 1) {
                throw new RuntimeException("Simulated temporary processing failure for order " + order.getOrderId());
            }
        }

        if ("invalid-order".equalsIgnoreCase(order.getProduct())) {
            throw new IllegalArgumentException("Simulated invalid order payload for " + order.getOrderId());
        }
    }
}
