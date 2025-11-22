package com.example.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    private final Kafka kafka = new Kafka();
    private final Topic topic = new Topic();
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Retry retry = new Retry();
    private String mode = "producer";

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
