package org.karimbkb.model;

import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.dto.KafkaMessage;

import java.util.List;
import java.util.Map;

public interface KafkaManagement {
  Map<String, List<PartitionInfo>> loadKafkaTopics();
  List<KafkaMessage> loadMessagesByTopic(String topic);
}
