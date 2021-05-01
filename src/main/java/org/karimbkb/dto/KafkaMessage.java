package org.karimbkb.dto;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class KafkaMessage {
  SimpleLongProperty offset;
  SimpleStringProperty message;
  SimpleStringProperty timestamp;

  public KafkaMessage(Long offset, String message, String timestamp) {
    this.offset = new SimpleLongProperty(offset);
    this.message = new SimpleStringProperty(message);
    this.timestamp = new SimpleStringProperty(timestamp);
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
