package org.karimbkb.model;

import com.google.inject.Inject;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dao.Database;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.kafka.Consumer;

import java.sql.SQLException;
import java.util.Properties;

public class Util {

  private final Database db;
  private final Consumer consumer;

  @Inject
  public Util(Database db, Consumer consumer) {
    this.db = db;
    this.consumer = consumer;
  }

  public Properties getPropertiesForCurrentProfile() {
    String currentSelectedProfile =
        KafkaPilot.kafkaPilotController.getCurrentSelectedItem().getText();
    KafkaConfig kafkaConfig;
    try {
      kafkaConfig = db.fetch(new KafkaConfig(currentSelectedProfile));
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Fetching current profile failed", e)
          .showAndWait();
      return new Properties();
    }
    return consumer.getProperties(kafkaConfig);
  }
}
