package com.example.kafka.config;

import com.example.kafka.properties.DemoProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public final class KafkaPropertiesFactory {

    private KafkaPropertiesFactory() {
    }

    public static Properties producer(DemoProperties properties) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put("schema.registry.url", properties.getKafka().getSchemaRegistryUrl());
        applySchemaRegistryAuthentication(props, properties);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, properties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getProducer().getRetries());
        props.put(ProducerConfig.LINGER_MS_CONFIG, properties.getProducer().getLingerMs());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getProducer().getBatchSize());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, properties.getProducer().getClientId());
        return props;
    }

    public static Properties consumer(DemoProperties properties) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put("schema.registry.url", properties.getKafka().getSchemaRegistryUrl());
        applySchemaRegistryAuthentication(props, properties);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getConsumer().getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getConsumer().getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("specific.avro.reader", false);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getConsumer().getMaxPollRecords());
        return props;
    }

    public static Properties retryProducer(Properties producerProps, DemoProperties properties) {
        Properties retryProps = new Properties();
        retryProps.putAll(producerProps);
        retryProps.put(ProducerConfig.CLIENT_ID_CONFIG, properties.getConsumer().getClientId());
        return retryProps;
    }

    public static Properties streams(DemoProperties properties) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, properties.getConsumer().getClientId() + "-streams");
        props.put("schema.registry.url", properties.getKafka().getSchemaRegistryUrl());
        applySchemaRegistryAuthentication(props, properties);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
        return props;
    }

    private static void applySchemaRegistryAuthentication(Properties props, DemoProperties properties) {
        String userInfo = properties.getKafka().getSchemaRegistryAuthUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            props.put("basic.auth.credentials.source", properties.getKafka().getSchemaRegistryAuthCredentialsSource());
            props.put("basic.auth.user.info", userInfo);
        }
    }
}
