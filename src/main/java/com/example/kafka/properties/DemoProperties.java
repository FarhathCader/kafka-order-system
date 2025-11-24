package com.example.kafka.properties;

import java.util.Properties;

public class DemoProperties {

    private final Kafka kafka = new Kafka();
    private final Topic topic = new Topic();
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Retry retry = new Retry();
    private String mode = "producer";

    public static DemoProperties from(Properties props) {
        DemoProperties config = new DemoProperties();

        String modeValue = props.getProperty("demo.mode");
        if (modeValue != null) {
            config.setMode(modeValue.trim());
        }

        Kafka kafka = config.getKafka();
        setIfPresent(props, "demo.kafka.bootstrap-servers", kafka::setBootstrapServers);
        setIfPresent(props, "demo.kafka.schema-registry-url", kafka::setSchemaRegistryUrl);
        setIfPresent(props, "demo.kafka.schema-registry-auth-credentials-source", kafka::setSchemaRegistryAuthCredentialsSource);
        setIfPresent(props, "demo.kafka.schema-registry-auth-user-info", kafka::setSchemaRegistryAuthUserInfo);

        Topic topic = config.getTopic();
        setIfPresent(props, "demo.topic.orders", topic::setOrders);
        setIfPresent(props, "demo.topic.retry", topic::setRetry);
        setIfPresent(props, "demo.topic.dlq", topic::setDlq);
        setIfPresent(props, "demo.topic.average", topic::setAverage);

        Producer producer = config.getProducer();
        setIfPresentInt(props, "demo.producer.linger-ms", producer::setLingerMs);
        setIfPresentInt(props, "demo.producer.batch-size", producer::setBatchSize);
        setIfPresentInt(props, "demo.producer.retries", producer::setRetries);
        setIfPresent(props, "demo.producer.acks", producer::setAcks);
        setIfPresent(props, "demo.producer.client-id", producer::setClientId);
        setIfPresentInt(props, "demo.producer.messages-per-second", producer::setMessagesPerSecond);

        Consumer consumer = config.getConsumer();
        setIfPresent(props, "demo.consumer.group-id", consumer::setGroupId);
        setIfPresent(props, "demo.consumer.client-id", consumer::setClientId);
        setIfPresentInt(props, "demo.consumer.max-poll-records", consumer::setMaxPollRecords);

        Retry retry = config.getRetry();
        setIfPresentInt(props, "demo.retry.max-attempts", retry::setMaxAttempts);
        setIfPresentLong(props, "demo.retry.initial-backoff-ms", retry::setInitialBackoffMs);

        return config;
    }

    private static void setIfPresent(Properties props, String key, java.util.function.Consumer<String> setter) {
        String value = props.getProperty(key);
        if (value != null) {
            setter.accept(value.trim());
        }
    }

    private static void setIfPresentInt(Properties props, String key, java.util.function.IntConsumer setter) {
        String value = props.getProperty(key);
        if (value != null) {
            setter.accept(Integer.parseInt(value.trim()));
        }
    }

    private static void setIfPresentLong(Properties props, String key, java.util.function.LongConsumer setter) {
        String value = props.getProperty(key);
        if (value != null) {
            setter.accept(Long.parseLong(value.trim()));
        }
    }

    public Kafka getKafka() {
        return kafka;
    }

    public Topic getTopic() {
        return topic;
    }

    public Producer getProducer() {
        return producer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public Retry getRetry() {
        return retry;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public static class Kafka {
        private String bootstrapServers = "localhost:9092";
        private String schemaRegistryUrl = "http://localhost:8081";
        private String schemaRegistryAuthCredentialsSource = "USER_INFO";
        private String schemaRegistryAuthUserInfo = "";

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getSchemaRegistryUrl() {
            return schemaRegistryUrl;
        }

        public void setSchemaRegistryUrl(String schemaRegistryUrl) {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        public String getSchemaRegistryAuthCredentialsSource() {
            return schemaRegistryAuthCredentialsSource;
        }

        public void setSchemaRegistryAuthCredentialsSource(String schemaRegistryAuthCredentialsSource) {
            this.schemaRegistryAuthCredentialsSource = schemaRegistryAuthCredentialsSource;
        }

        public String getSchemaRegistryAuthUserInfo() {
            return schemaRegistryAuthUserInfo;
        }

        public void setSchemaRegistryAuthUserInfo(String schemaRegistryAuthUserInfo) {
            this.schemaRegistryAuthUserInfo = schemaRegistryAuthUserInfo;
        }
    }

    public static class Topic {
        private String orders = "orders";
        private String retry = "orders-retry";
        private String dlq = "orders-dlq";
        private String average = "orders-average";

        public String getOrders() {
            return orders;
        }

        public void setOrders(String orders) {
            this.orders = orders;
        }

        public String getRetry() {
            return retry;
        }

        public void setRetry(String retry) {
            this.retry = retry;
        }

        public String getDlq() {
            return dlq;
        }

        public void setDlq(String dlq) {
            this.dlq = dlq;
        }

        public String getAverage() {
            return average;
        }

        public void setAverage(String average) {
            this.average = average;
        }
    }

    public static class Producer {
        private String clientId = "order-producer";
        private int lingerMs = 20;
        private int batchSize = 16384;
        private int retries = 5;
        private String acks = "all";
        private int messagesPerSecond = 1;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public int getLingerMs() {
            return lingerMs;
        }

        public void setLingerMs(int lingerMs) {
            this.lingerMs = lingerMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public String getAcks() {
            return acks;
        }

        public void setAcks(String acks) {
            this.acks = acks;
        }

        public int getMessagesPerSecond() {
            return messagesPerSecond;
        }

        public void setMessagesPerSecond(int messagesPerSecond) {
            this.messagesPerSecond = messagesPerSecond;
        }
    }

    public static class Consumer {
        private String groupId = "order-consumer-group";
        private String clientId = "order-consumer";
        private int maxPollRecords = 50;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public int getMaxPollRecords() {
            return maxPollRecords;
        }

        public void setMaxPollRecords(int maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialBackoffMs = 500;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialBackoffMs() {
            return initialBackoffMs;
        }

        public void setInitialBackoffMs(long initialBackoffMs) {
            this.initialBackoffMs = initialBackoffMs;
        }
    }
}
