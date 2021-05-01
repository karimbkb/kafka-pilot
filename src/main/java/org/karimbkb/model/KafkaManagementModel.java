package org.karimbkb.model;

import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dao.Database;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;

import java.util.*;
import java.util.stream.Collectors;

// TODO: divide KafkaManagementModel into KafkaConsumerModel and KafkaProducerModel
public class KafkaManagementModel implements KafkaManagement {

  private final Database db;

  @Inject
  public KafkaManagementModel(Database db) {
    this.db = db;
  }

  public Map<String, List<PartitionInfo>> loadKafkaTopics() {
    final KafkaConfig kafkaConfig = getCurrentKafkaConfig();
    final Properties props = getProperties(kafkaConfig);

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    Map<String, List<PartitionInfo>> topics = consumer.listTopics();
    consumer.close();

    return topics;
  }

  public List<KafkaMessage> loadMessagesByTopic(String topic) {
    try (KafkaConsumer<String, String> consumer =
        new KafkaConsumer<>(getProperties(getCurrentKafkaConfig()))) {
      List<PartitionInfo> partitionsInfo = consumer.partitionsFor(topic);
      List<TopicPartition> topicPartitions =
          partitionsInfo.stream()
              .map(
                  partitionInfo ->
                      new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
              .collect(Collectors.toList());

//      Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions) - 100L;
//        consumer.seek(Arrays.asList(topicPartition));


      List<KafkaMessage> kafkaMessages = new ArrayList<>();
      topicPartitions.forEach(topicPartition -> {
        consumer.assign(Collections.singleton(topicPartition));
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records)
//          System.out.printf(
//                  "offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        kafkaMessages.add(new KafkaMessage(record.offset(), record.value(), ""));
      });

      return kafkaMessages;

    } catch (Exception e) {

    }

    return null;
  }

  private Properties getProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
  }

  private KafkaConfig getCurrentKafkaConfig() {
    String currentSelectedProfile =
        KafkaPilot.getKafkaPilotController().getCurrentSelectedItem().getText();
    final KafkaConfig kafkaConfig = db.fetch(new KafkaConfig(currentSelectedProfile));
    return kafkaConfig;
  }
}
