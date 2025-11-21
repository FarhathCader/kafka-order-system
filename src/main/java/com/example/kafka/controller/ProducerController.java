package com.example.kafka.controller;

import com.example.kafka.config.KafkaPropertiesFactory;
import com.example.kafka.config.SchemaLoader;
import com.example.kafka.properties.DemoProperties;
import com.example.kafka.service.OrderProducerService;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ProducerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerController.class);

    private final DemoProperties demoProperties;

    public ProducerController(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    public void start() throws Exception {
        Schema schema = SchemaLoader.load("order.avsc");
        Properties producerProps = KafkaPropertiesFactory.producer(demoProperties);

        OrderProducerService producerService = new OrderProducerService(
                producerProps,
                schema,
                demoProperties.getTopic().getOrders(),
                demoProperties.getProducer().getMessagesPerSecond()
        );

        LOGGER.info("Bootstrapping producer controller for topic {}", demoProperties.getTopic().getOrders());
        producerService.startProducing();
    }
}
