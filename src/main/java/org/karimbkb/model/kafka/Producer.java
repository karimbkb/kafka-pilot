package org.karimbkb.model.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
import java.util.Properties;

public interface Producer {
  void produceMessage(String topic, String message) throws SQLException;

  default Properties getProducerProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
  }
}
