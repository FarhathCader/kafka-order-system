package com.assignment.kafka.producer;

import com.assignment.kafka.avro.Order;
import com.assignment.kafka.util.ProducerUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class OrderProducer {

    private static final String TOPIC = "orders";

    public static void main(String[] args) throws Exception {
        // Producer with key=String, value=Order
        KafkaProducer<String, Order> producer = ProducerUtil.buildProducer();

        for (int i = 1; i <= 20; i++) {
            // Avro Order (String is fine, Avro expects CharSequence and String implements it)
            Order order = new Order("ID-" + i, "Item" + i, (float) (Math.random() * 100));

            String key = order.getOrderId().toString();

            ProducerRecord<String, Order> record =
                    new ProducerRecord<String, Order>(TOPIC, key, order);


            producer.send(record);
            System.out.println("Sent: " + order);
            Thread.sleep(500);
        }

        producer.flush();
        producer.close();
        System.out.println("Producer finished.");
    }
}
