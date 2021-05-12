package org.karimbkb.model;

import com.google.inject.Inject;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.karimbkb.KafkaPilot;
import org.karimbkb.controller.KafkaManagementController;
import org.karimbkb.dao.Database;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
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

    KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
    Map<String, List<PartitionInfo>> topics = consumer.listTopics();
    consumer.close();

    return topics;
  }

  public List<KafkaMessage> loadMessagesByTopic(String topic) {
    try (KafkaConsumer<Long, String> consumer =
        new KafkaConsumer<>(getProperties(getCurrentKafkaConfig()))) {
      List<PartitionInfo> partitionsInfo = consumer.partitionsFor(topic);
      List<TopicPartition> topicPartitions =
          partitionsInfo.stream()
              .map(
                  partitionInfo ->
                      new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
              .collect(Collectors.toList());

      int setOffset =
          Integer.parseInt(KafkaPilot.registry.getData(KafkaManagementController.MAX_DISPLAY_MESSAGES))
              / topicPartitions.size();

      long endOffsets =
          Math.max(
              consumer.endOffsets(topicPartitions).entrySet().stream().findFirst().get().getValue()
                  - Integer.toUnsignedLong(setOffset),
              0L);
      List<KafkaMessage> kafkaMessages = new ArrayList<>();
      topicPartitions.forEach(
          topicPartition -> {
            consumer.assign(Collections.singleton(topicPartition));
            consumer.seek(topicPartition, endOffsets);
            ConsumerRecords<Long, String> records = consumer.poll(100);

            for (ConsumerRecord<Long, String> record : records)
              kafkaMessages.add(
                  new KafkaMessage(
                      record.topic(),
                      record.partition(),
                      record.offset(),
                      record.value(),
                      Long.toString(record.timestamp())));
          });

      return kafkaMessages;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public void produceMessage(String topic, String message) {
    try (KafkaProducer<Long, String> producer =
        new KafkaProducer<>(getProducerProperties(getCurrentKafkaConfig()))) {
      ProducerRecord<Long, String> record = new ProducerRecord<>(topic, message);
      producer.send(record);
    } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
      Notification.createExceptionAlert("Error", "Producing message failed", e).showAndWait();
    } catch (KafkaException e) {
      Notification.createExceptionAlert("Error", "Deleting topic failed", e).showAndWait();
    }
  }

  public void produceAvroMessage(String topic, String message) {
    try (KafkaProducer<Long, Object> producer =
                 new KafkaProducer<>(getAvroProducerProperties(getCurrentKafkaConfig()))) {
      Long key = 1L;
      String userSchema = "{\"type\":\"record\"," +
              "\"name\":\"myrecord\"," +
              "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";

      Schema.Parser parser = new Schema.Parser();
      Schema schema = parser.parse(userSchema);
      GenericRecord avroRecord = new GenericData.Record(schema);
      avroRecord.put("f1", "value1");

      ProducerRecord<Long, Object> record = new ProducerRecord<>("topic1", key, avroRecord);

        producer.send(record);
    } catch(SerializationException e) {
      // TODO: Add alert for serialization exception
    } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
      // TODO: add alert that an fatal error has occurred
    } catch (KafkaException e) {
      // TODO: add alert window
    }
  }

  public Properties getProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
  }

  private Properties getProducerProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
  }

  private Properties getAvroProducerProperties(KafkaConfig kafkaConfig) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServer());
    props.put("schema.registry.url", "http://localhost:8081");
    props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfig.getGroupId());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    return props;
  }

  private KafkaConfig getCurrentKafkaConfig() {
    String currentSelectedProfile =
        KafkaPilot.kafkaPilotController.getCurrentSelectedItem().getText();
      try {
          return db.fetch(new KafkaConfig(currentSelectedProfile));
      } catch (SQLException e) {
          Notification.createExceptionAlert("Error", "Fetching curent config failed", e).showAndWait();
      }
      return null;
  }
}
