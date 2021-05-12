package org.karimbkb.model;

import com.google.inject.Inject;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dao.Database;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
import java.util.Properties;

public class Util {

  private final Database db;
  private final KafkaManagement kafkaManagement;

  @Inject
  public Util(Database db, KafkaManagement kafkaManagement) {
    this.db = db;
    this.kafkaManagement = kafkaManagement;
  }

  public Properties getPropertiesForCurrentProfile() {
    String currentSelectedProfile =
        KafkaPilot.kafkaPilotController.getCurrentSelectedItem().getText();
    KafkaConfig kafkaConfig = null;
    try {
      kafkaConfig = db.fetch(new KafkaConfig(currentSelectedProfile));
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Fetching current profile failed", e).showAndWait();
    }
    return kafkaManagement.getProperties(kafkaConfig);
  }
}
