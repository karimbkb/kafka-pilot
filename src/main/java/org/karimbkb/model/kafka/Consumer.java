package org.karimbkb.model.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.karimbkb.controller.KafkaManagementController;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface Consumer {
  Map<String, List<PartitionInfo>> loadKafkaTopics() throws SQLException;
  List<KafkaMessage> loadMessagesByTopic(String topic) throws SQLException;
  void setKafkaManagementController(KafkaManagementController kafkaManagementController);
  KafkaManagementController getKafkaManagementController();

  default Properties getProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
    return props;
  }

  default Properties getAvroConsumerProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put("schema.registry.url", kafkaConfig.getSchemaRegistryUrl());
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);

    return props;
  }
}
