package com.example.kafka;

import com.example.kafka.config.KafkaPropertiesFactory;
import com.example.kafka.config.PropertiesLoader;
import com.example.kafka.config.SchemaLoader;
import com.example.kafka.properties.DemoProperties;
import com.example.kafka.service.OrderProducerService;
import org.apache.avro.Schema;

import java.util.Properties;
import java.util.logging.Logger;

public final class ProducerMain {
    private static final Logger LOGGER = Logger.getLogger(ProducerMain.class.getName());

    private ProducerMain() {
    }

    public static void main(String[] args) throws Exception {
        Properties fileProps = PropertiesLoader.load("application.properties");
        DemoProperties demoProperties = DemoProperties.from(fileProps);

        Schema schema = SchemaLoader.load("order.avsc");
        Properties producerProps = KafkaPropertiesFactory.producer(demoProperties);

        OrderProducerService producerService = new OrderProducerService(
                producerProps,
                schema,
                demoProperties.getTopic().getOrders(),
                demoProperties.getProducer().getMessagesPerSecond()
        );

        LOGGER.info(() -> "Starting plain Java producer on topic " + demoProperties.getTopic().getOrders());
        producerService.startProducing();
    }
}
