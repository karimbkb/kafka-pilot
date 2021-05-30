package org.karimbkb.model.kafka;

import com.google.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.Notification;

import java.sql.SQLException;

public class Writer implements org.karimbkb.model.kafka.Producer {
  private final KafkaCommon common;

  @Inject
  public Writer(KafkaCommon common) {
    this.common = common;
  }

  @Override
  public void produceMessage(String topic, String message) throws SQLException {
    KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();
    try (KafkaProducer<Long, String> producer =
        new KafkaProducer<>(getProducerProperties(kafkaConfig))) {
      ProducerRecord<Long, String> record = new ProducerRecord<>(topic, message);
      producer.send(record);
    } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
      Notification.createExceptionAlert("Error", "Producing message failed", e).showAndWait();
    } catch (KafkaException e) {
      Notification.createExceptionAlert("Error", "General Kafka error", e).showAndWait();
    }
  }
}
