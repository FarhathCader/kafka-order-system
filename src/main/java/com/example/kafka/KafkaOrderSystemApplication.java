package com.example.kafka;

import com.example.kafka.controller.ConsumerController;
import com.example.kafka.controller.ProducerController;
import com.example.kafka.properties.DemoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DemoProperties.class)
public class KafkaOrderSystemApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOrderSystemApplication.class);

    private final ProducerController producerController;
    private final ConsumerController consumerController;
    private final DemoProperties demoProperties;

    public KafkaOrderSystemApplication(ProducerController producerController,
                                       ConsumerController consumerController,
                                       DemoProperties demoProperties) {
        this.producerController = producerController;
        this.consumerController = consumerController;
        this.demoProperties = demoProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaOrderSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String mode = demoProperties.getMode().toLowerCase();
        LOGGER.info("Starting Kafka order system in {} mode", mode);

        switch (mode) {
            case "producer" -> startProducer();
            case "consumer" -> startConsumer();
            case "both" -> {
                startProducer();
                startConsumer();
            }
            default -> throw new IllegalArgumentException("Unknown demo.mode value: " + mode);
        }
    }

    private void startProducer() {
        Thread producerThread = new Thread(() -> {
            try {
                producerController.start();
            } catch (Exception e) {
                LOGGER.error("Producer failed", e);
            }
        }, "order-producer-thread");
        producerThread.start();
        LOGGER.info("Producer thread started");
    }

    private void startConsumer() {
        Thread consumerThread = new Thread(() -> {
            try {
                consumerController.start();
            } catch (Exception e) {
                LOGGER.error("Consumer failed", e);
            }
        }, "order-consumer-thread");
        consumerThread.start();
        LOGGER.info("Consumer thread started");
    }
}
