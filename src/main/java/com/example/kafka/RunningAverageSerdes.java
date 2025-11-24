package com.example.kafka;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Serdes;

import java.nio.ByteBuffer;

public final class RunningAverageSerdes {
    private RunningAverageSerdes() {
    }

    public static Serde<RunningAverage> instance() {
        return Serdes.serdeFrom(new RunningAverageSerializer(), new RunningAverageDeserializer());
    }

    private static final class RunningAverageSerializer implements Serializer<RunningAverage> {
        @Override
        public byte[] serialize(String topic, RunningAverage data) {
            if (data == null) {
                return new byte[0];
            }
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES + Long.BYTES);
            buffer.putDouble(data.getTotal());
            buffer.putLong(data.getCount());
            return buffer.array();
        }
    }

    private static final class RunningAverageDeserializer implements Deserializer<RunningAverage> {
        @Override
        public RunningAverage deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return new RunningAverage();
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            double total = buffer.getDouble();
            long count = buffer.getLong();
            return new RunningAverage(total, count);
        }
    }
}
