package com.example.kafka.service;

import com.example.kafka.model.Order;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class OrderProducerService {
    private static final Logger LOGGER = Logger.getLogger(OrderProducerService.class.getName());

    private final Properties producerProps;
    private final Schema schema;
    private final String topic;
    private final int messagesPerSecond;
    private final Random random = new Random();
    private final String[] products = new String[]{"keyboard", "mouse", "monitor", "laptop", "dock"};
    private final AtomicInteger orderCounter = new AtomicInteger(0);

    public OrderProducerService(Properties producerProps, Schema schema, String topic, int messagesPerSecond) {
        this.producerProps = producerProps;
        this.schema = schema;
        this.topic = topic;
        this.messagesPerSecond = messagesPerSecond;
    }

    public void startProducing() throws InterruptedException {
        try (KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(producerProps)) {
            LOGGER.info(() -> "Starting producer on topic " + topic + " at " + messagesPerSecond + " msg/sec");

            for (int i = 0; i < 10; i++) {
                int orderNumber = orderCounter.incrementAndGet();
                sendOrder(producer, randomOrder(), "order " + orderNumber);
            }

            Order temporaryFailureOrder = new Order(UUID.randomUUID().toString(), "temporary-failure", 199.99);
            LOGGER.info(() -> "Submitted temporary-failure order " + shortId(temporaryFailureOrder.getOrderId()));
            sendOrder(producer, temporaryFailureOrder, "temporary-failure order " + shortId(temporaryFailureOrder.getOrderId()));

            Order invalidOrder = new Order(UUID.randomUUID().toString(), "invalid-order", 0.0);
            LOGGER.info(() -> "Submitted invalid-order " + shortId(invalidOrder.getOrderId()));
            sendOrder(producer, invalidOrder, "invalid-order " + shortId(invalidOrder.getOrderId()));

            producer.flush();
            LOGGER.info("Finished sending demo orders.");

        }
    }

    private Order randomOrder() {
        String product = products[random.nextInt(products.length)];
        double price = 50 + (250 * random.nextFloat());
        return new Order(UUID.randomUUID().toString(), product, price);
    }

    private void sendOrder(KafkaProducer<String, GenericRecord> producer, Order order, String label) throws InterruptedException {
        GenericRecord record = order.toGenericRecord(schema);

        ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<>(topic, order.getOrderId(), record);
        producer.send(producerRecord, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.severe(() -> "Failed to send " + label + ": " + exception.getMessage());
            } else {
                LOGGER.info(() -> "Sent " + label + " → offset " + metadata.offset());
            }
        });

        Thread.sleep(Duration.ofSeconds(1).toMillis() / Math.max(1, messagesPerSecond));
    }

    private String shortId(String id) {
        if (id == null || id.length() <= 4) {
            return id;
        }
        return id.substring(0, 4) + "...";
    }
}
