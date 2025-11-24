package com.example.kafka;

import com.example.kafka.config.KafkaPropertiesFactory;
import com.example.kafka.config.PropertiesLoader;
import com.example.kafka.properties.DemoProperties;
import com.example.kafka.service.AverageAggregator;
import com.example.kafka.service.DlqService;
import com.example.kafka.service.OrderConsumerService;
import com.example.kafka.service.RetryService;
import com.example.kafka.config.LoggingConfigurator;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

public final class ConsumerMain {
    private static final Logger LOGGER = Logger.getLogger(ConsumerMain.class.getName());

    private ConsumerMain() {
    }

    public static void main(String[] args) throws Exception {
        LoggingConfigurator.configure();
        Properties fileProps = PropertiesLoader.load("application.properties");
        DemoProperties demoProperties = DemoProperties.from(fileProps);

        Properties consumerProps = KafkaPropertiesFactory.consumer(demoProperties);
        Properties producerProps = KafkaPropertiesFactory.producer(demoProperties);
        Properties retryProducerProps = KafkaPropertiesFactory.retryProducer(producerProps, demoProperties);

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, Object> dlqProducer = new KafkaProducer<>(producerProps);
             KafkaProducer<String, Object> retryProducer = new KafkaProducer<>(retryProducerProps)) {

            DlqService dlqService = new DlqService(dlqProducer, demoProperties.getTopic().getDlq());
            RetryService retryService = new RetryService(
                    demoProperties.getRetry().getMaxAttempts(),
                    demoProperties.getRetry().getInitialBackoffMs(),
                    demoProperties.getTopic().getRetry(),
                    dlqService,
                    retryProducer
            );

            OrderConsumerService consumerService = new OrderConsumerService(
                    consumer,
                    retryService,
                    new AverageAggregator(),
                    demoProperties.getTopic().getOrders(),
                    demoProperties.getTopic().getRetry()
            );

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown requested. Stopping consumer loop.");
                consumerService.shutdown();
                retryProducer.close(Duration.ofSeconds(5));
                dlqProducer.close(Duration.ofSeconds(5));
            }));

            LOGGER.info(() -> "Starting consumer for topics "
                    + demoProperties.getTopic().getOrders() + " and " + demoProperties.getTopic().getRetry());
            consumerService.start();
        }
    }
}
