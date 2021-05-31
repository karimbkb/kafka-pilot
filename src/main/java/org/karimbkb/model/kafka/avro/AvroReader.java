package org.karimbkb.model.kafka.avro;

import com.google.inject.Inject;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.karimbkb.KafkaPilot;
import org.karimbkb.controller.KafkaManagementController;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.kafka.Common;
import org.karimbkb.model.kafka.Consumer;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AvroReader implements Consumer {
  private final Common common;
  private KafkaManagementController kafkaManagementController;

  @Inject
  public AvroReader(Common common) {
    this.common = common;
  }

  @Override
  public Map<String, List<PartitionInfo>> loadKafkaTopics() throws SQLException {
    final KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();
    final Properties props = getProperties(kafkaConfig);

    KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
    Map<String, List<PartitionInfo>> topics = consumer.listTopics();
    consumer.close();

    return topics;
  }

  public List<KafkaMessage> loadMessagesByTopic(String topic) throws SQLException {
    KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();
    try (KafkaConsumer<Long, GenericRecord> consumer =
        new KafkaConsumer<>(getAvroConsumerProperties(kafkaConfig))) {
      List<PartitionInfo> partitionsInfo = consumer.partitionsFor(topic);
      List<TopicPartition> topicPartitions =
          partitionsInfo.stream()
              .map(
                  partitionInfo ->
                      new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
              .collect(Collectors.toList());

      int setOffset = calcMaxOffsetPerPartition(topicPartitions);
      long endOffsets = calcOffset(consumer, topicPartitions, setOffset);

      List<KafkaMessage> kafkaMessages = new ArrayList<>();
      topicPartitions.forEach(
          topicPartition -> {
            consumer.assign(Collections.singleton(topicPartition));
            if(endOffsets > 0) {
              consumer.seek(topicPartition, endOffsets);
            } else {
              consumer.seekToBeginning(Collections.singleton(topicPartition));
            }

            ConsumerRecords<Long, GenericRecord> records = consumer.poll(100);
            getKafkaManagementController().getProgressBar().setProgress((float) topicPartition.partition() / topicPartitions.size());

            for (ConsumerRecord<Long, GenericRecord> record : records)
              kafkaMessages.add(
                  new KafkaMessage(
                      record.topic(),
                      record.partition(),
                      record.offset(),
                      record.value().toString(),
                      Long.toString(record.timestamp())));
          });

      return kafkaMessages;
    }
  }

  public void setKafkaManagementController(KafkaManagementController kafkaManagementController) {
    this.kafkaManagementController = kafkaManagementController;
  }

  public KafkaManagementController getKafkaManagementController() {
    return kafkaManagementController;
  }


  private long calcOffset(
      KafkaConsumer<Long, GenericRecord> consumer, List<TopicPartition> topicPartitions, int setOffset) {
    return Math.max(
        consumer.endOffsets(topicPartitions).entrySet().stream().findFirst().get().getValue()
            - Integer.toUnsignedLong(setOffset),
        0L);
  }

  private int calcMaxOffsetPerPartition(List<TopicPartition> topicPartitions) {
    return Integer.parseInt(
            KafkaPilot.registry.getData(KafkaManagementController.MAX_DISPLAY_MESSAGES))
        / topicPartitions.size();
  }
}
