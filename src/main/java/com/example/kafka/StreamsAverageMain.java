package com.example.kafka;

import com.example.kafka.config.KafkaPropertiesFactory;
import com.example.kafka.config.LoggingConfigurator;
import com.example.kafka.config.PropertiesLoader;
import com.example.kafka.config.SchemaLoader;
import com.example.kafka.properties.DemoProperties;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.streams.KeyValue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public final class StreamsAverageMain {
    private static final Logger LOGGER = Logger.getLogger(StreamsAverageMain.class.getName());

    private StreamsAverageMain() {
    }

//    public static void main(String[] args) throws Exception {
//        Properties props = PropertiesLoader.load("application.properties");
//        DemoProperties demoProperties = DemoProperties.from(props);
//        SchemaLoader.load("order.avsc");
//
//        Properties streamsProps = KafkaPropertiesFactory.streams(demoProperties);
//        Topology topology = buildTopology(demoProperties);
//
//        try (KafkaStreams streams = new KafkaStreams(topology, streamsProps)) {
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                LOGGER.info("Stopping Kafka Streams application");
//                streams.close(Duration.ofSeconds(5));
//            }));
//
//            LOGGER.info(() -> "Starting Kafka Streams running average on " + demoProperties.getTopic().getOrders());
//            streams.start();
//        }
//    }
public static void main(String[] args) throws Exception {
    LoggingConfigurator.configure();
    Properties props = PropertiesLoader.load("application.properties");
    DemoProperties demoProperties = DemoProperties.from(props);
    SchemaLoader.load("order.avsc");

    Properties streamsProps = KafkaPropertiesFactory.streams(demoProperties);
    Topology topology = buildTopology(demoProperties);

    KafkaStreams streams = new KafkaStreams(topology, streamsProps);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        LOGGER.info("Stopping Kafka Streams application");
        streams.close(Duration.ofSeconds(5));
    }));

    LOGGER.info(() -> "Starting Kafka Streams running average on " + demoProperties.getTopic().getOrders());

    streams.start();

    // --- BLOCK FOREVER (required for Kafka Streams 2.x) ---
    Thread.sleep(Long.MAX_VALUE);
}

    private static Topology buildTopology(DemoProperties demoProperties) {
        StreamsBuilder builder = new StreamsBuilder();
        GenericAvroSerde avroSerde = createSerde(demoProperties);

        KStream<String, GenericRecord> orders = builder.stream(
                demoProperties.getTopic().getOrders(),
                Consumed.with(Serdes.String(), avroSerde)
        );

        KTable<String, RunningAverage> averages = orders
                .map((key, record) -> KeyValue.pair(
                        record.get("product").toString(),
                        Double.parseDouble(record.get("price").toString())
                ))
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Double()))
                .aggregate(
                        RunningAverage::new,
                        (product, price, aggregate) -> aggregate.add(price),
                        Materialized.with(Serdes.String(), RunningAverageSerdes.instance())
                );

        averages.toStream()
                .mapValues(RunningAverage::average)
                .to(demoProperties.getTopic().getAverage(), Produced.with(Serdes.String(), Serdes.Double()));

        return builder.build();
    }

    private static GenericAvroSerde createSerde(DemoProperties demoProperties) {
        GenericAvroSerde serde = new GenericAvroSerde();
        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", demoProperties.getKafka().getSchemaRegistryUrl());
        if (!demoProperties.getKafka().getSchemaRegistryAuthUserInfo().isBlank()) {
            config.put("basic.auth.credentials.source", demoProperties.getKafka().getSchemaRegistryAuthCredentialsSource());
            config.put("basic.auth.user.info", demoProperties.getKafka().getSchemaRegistryAuthUserInfo());
        }
        serde.configure(config, false);
        return serde;
    }
}
