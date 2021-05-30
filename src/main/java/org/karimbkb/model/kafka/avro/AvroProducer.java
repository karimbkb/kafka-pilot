package org.karimbkb.model.kafka.avro;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.karimbkb.entity.KafkaConfig;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Properties;

public interface AvroProducer {
  void produceAvroMessage(String topic, String message, String schemaStr) throws SQLException;

  String readSchemaFile(String path) throws FileNotFoundException;

  default Properties getAvroProducerProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put("schema.registry.url", kafkaConfig.getSchemaRegistryUrl());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    return props;
  }
}
