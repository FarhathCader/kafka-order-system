package com.example.kafka.controller;

import com.example.kafka.config.KafkaPropertiesFactory;
import com.example.kafka.properties.DemoProperties;
import com.example.kafka.service.AverageAggregator;
import com.example.kafka.service.DlqService;
import com.example.kafka.service.OrderConsumerService;
import com.example.kafka.service.RetryService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ConsumerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerController.class);

    private final DemoProperties demoProperties;

    public ConsumerController(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    public void start() throws Exception {
        Properties consumerProps = KafkaPropertiesFactory.consumer(demoProperties);
        Properties producerProps = KafkaPropertiesFactory.producer(demoProperties);
        Properties retryProducerProps = KafkaPropertiesFactory.retryProducer(producerProps, demoProperties);

        String ordersTopic = demoProperties.getTopic().getOrders();
        String retryTopic = demoProperties.getTopic().getRetry();
        String dlqTopic = demoProperties.getTopic().getDlq();

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, Object> dlqProducer = new KafkaProducer<>(producerProps);
             KafkaProducer<String, Object> retryProducer = new KafkaProducer<>(retryProducerProps)) {

            DlqService dlqService = new DlqService(dlqProducer, dlqTopic);
            RetryService retryService = new RetryService(
                    demoProperties.getRetry().getMaxAttempts(),
                    demoProperties.getRetry().getInitialBackoffMs(),
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
