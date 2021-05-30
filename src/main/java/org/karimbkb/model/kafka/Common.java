package org.karimbkb.model.kafka;

import com.google.inject.Inject;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dao.Database;
import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;

public class Common implements KafkaCommon {

  private final Database db;

  @Inject
  public Common(Database db) {
    this.db = db;
  }

  public KafkaConfig getCurrentKafkaConfig() throws SQLException {
    String currentSelectedProfile =
        KafkaPilot.kafkaPilotController.getCurrentSelectedItem().getText();
    return db.fetch(new KafkaConfig(currentSelectedProfile));
  }
}
