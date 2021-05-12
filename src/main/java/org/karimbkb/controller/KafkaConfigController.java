package org.karimbkb.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dao.Database;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.Notification;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class KafkaConfigController implements Initializable {

  private final Database db;

  @FXML private TextField profileName;
  @FXML private TextField bootstrapServer;
  @FXML private TextField kafkaGroupId;
  @FXML private ListView<String> profileList;

  @Inject
  public KafkaConfigController(Database db) {
    this.db = db;
  }

  @SneakyThrows
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    initProfileList();
  }

  private void initProfileList() {
    List<KafkaConfig> kafkaConfigCollection = null;
    try {
      kafkaConfigCollection = db.fetchAll();
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Initialize profiles failed", e).showAndWait();
    }

    assert kafkaConfigCollection != null;
    for (KafkaConfig kafkaConfig : kafkaConfigCollection) {
      profileList.getItems().add(kafkaConfig.getProfileName());
    }

    profileList.setOnMouseClicked(
        click -> {
          if (click.getClickCount() == 2) {
            try {
              connect();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
  }

  @FXML
  private void saveConfigAndConnect() throws IOException {
    KafkaConfig kafkaConfig =
        new KafkaConfig(profileName.getText(), bootstrapServer.getText(), kafkaGroupId.getText());
    try {
      db.createTable();
      db.insert(kafkaConfig);
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Save config and connect failed", e).showAndWait();
    }

    KafkaPilot.kafkaPilotController.switchToKafkaManagementView(kafkaConfig);
  }

  @FXML
  private void connect() throws IOException {
    String selectedProfile = profileList.getSelectionModel().getSelectedItem();
    KafkaConfig kafkaConfig = null;
    try {
      kafkaConfig = db.fetch(new KafkaConfig(selectedProfile));
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Connecting to database failed", e).showAndWait();
    }

    assert kafkaConfig != null;
    KafkaPilot.kafkaPilotController.switchToKafkaManagementView(kafkaConfig);
  }

  @FXML
  private void deleteProfile() {
    String selectedProfile = profileList.getSelectionModel().getSelectedItem();
    try {
      db.delete(new KafkaConfig(selectedProfile));
      profileList.getItems().removeAll(selectedProfile);
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Deleting topic failed", e).showAndWait();
    }
  }
}
