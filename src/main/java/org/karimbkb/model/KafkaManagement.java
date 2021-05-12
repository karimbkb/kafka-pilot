package org.karimbkb.model;

import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface KafkaManagement {
  Map<String, List<PartitionInfo>> loadKafkaTopics();
  List<KafkaMessage> loadMessagesByTopic(String topic);
  void produceMessage(String topic, String message);
  Properties getProperties(KafkaConfig kafkaConfig);
}
