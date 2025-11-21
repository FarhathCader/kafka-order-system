package com.kafka.controller;

import com.kafka.config.KafkaPropertiesFactory;
import com.kafka.config.PropertiesLoader;
import com.kafka.service.AverageAggregator;
import com.kafka.service.DlqService;
import com.kafka.service.OrderConsumerService;
import com.kafka.service.RetryService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ConsumerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerController.class);

    public static void main(String[] args) throws Exception {
        Properties properties = PropertiesLoader.load("application.properties");
        Properties consumerProps = KafkaPropertiesFactory.consumer(properties);
        Properties producerProps = KafkaPropertiesFactory.producer(properties);
        Properties retryProducerProps = KafkaPropertiesFactory.retryProducer(producerProps, properties);

        String ordersTopic = properties.getProperty("demo.topic.orders", "orders");
        String retryTopic = properties.getProperty("demo.topic.retry", "orders-retry");
        String dlqTopic = properties.getProperty("demo.topic.dlq", "orders-dlq");

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, Object> dlqProducer = new KafkaProducer<>(producerProps);
             KafkaProducer<String, Object> retryProducer = new KafkaProducer<>(retryProducerProps)) {

            DlqService dlqService = new DlqService(dlqProducer, dlqTopic);
            RetryService retryService = new RetryService(
                    Integer.parseInt(properties.getProperty("demo.retry.maxAttempts", "3")),
                    Long.parseLong(properties.getProperty("demo.retry.initialBackoffMs", "500")),
                    retryTopic,
                    dlqService,
                    retryProducer
            );

            OrderConsumerService consumerService = new OrderConsumerService(
                    consumer,
                    retryService,
                    new AverageAggregator(),
                    ordersTopic,
                    retryTopic
            );

            Runtime.getRuntime().addShutdownHook(new Thread(consumerService::shutdown));

            LOGGER.info("Consumer controller bootstrapped for topics {}, {}", ordersTopic, retryTopic);
            consumerService.start();
        }
    }
}
