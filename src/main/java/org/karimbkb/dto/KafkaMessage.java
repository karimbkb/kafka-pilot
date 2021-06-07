package org.karimbkb.dto;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

@SuppressWarnings("unused")
public class KafkaMessage {
  final SimpleStringProperty topic;
  final SimpleIntegerProperty partition;
  final SimpleLongProperty offset;
  final SimpleStringProperty message;
  final SimpleStringProperty timestamp;

  public KafkaMessage(String topic, int partition, Long offset, String message, String timestamp) {
    this.topic = new SimpleStringProperty(topic);
    this.partition = new SimpleIntegerProperty(partition);
    this.offset = new SimpleLongProperty(offset);
    this.message = new SimpleStringProperty(message);
    this.timestamp = new SimpleStringProperty(timestamp);
  }

  public String getTopic() {
    return topic.get();
  }

  public void setTopic(String topic) {
    this.topic.set(topic);
  }

  public int getPartition() {
    return partition.get();
  }

  public void setPartition(int offset) {
    this.partition.set(offset);
  }

  public Long getOffset() {
    return offset.get();
  }

  public void setOffset(Long offset) {
    this.offset.set(offset);
  }

  public String getMessage() {
    return message.get();
  }

  public void setMessage(String message) {
    this.message.set(message);
  }

  public String getTimestamp() {
    return timestamp.get();
  }

  public void setTimestamp(String timestamp) {
    this.timestamp.set(timestamp);
  }
}
