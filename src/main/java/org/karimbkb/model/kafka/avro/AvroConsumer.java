package org.karimbkb.model.kafka.avro;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public interface AvroConsumer {
    List<KafkaMessage> loadMessagesByTopic(String topic) throws SQLException;

    default Properties getAvroConsumerProperties(KafkaConfig kafkaConfig) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
        props.put("schema.registry.url", kafkaConfig.getSchemaRegistryUrl());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
        return props;
    }
}
