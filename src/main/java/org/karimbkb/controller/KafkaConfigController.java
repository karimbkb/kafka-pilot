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

import java.io.IOException;
import java.net.URL;
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
    List<KafkaConfig> kafkaConfigCollection = db.fetchAll();

    for (KafkaConfig kafkaConfig : kafkaConfigCollection) {
      profileList.getItems().add(kafkaConfig.getProfileName());
    }
  }

  @FXML
  private void saveConfigAndConnect() throws IOException {
    KafkaConfig kafkaConfig =
        new KafkaConfig(profileName.getText(), bootstrapServer.getText(), kafkaGroupId.getText());
    db.createTable();
    db.insert(kafkaConfig);

    KafkaPilot.getKafkaPilotController().switchToKafkaManagementView(kafkaConfig);
  }

  @FXML
  private void connect() throws IOException {
    String selectedProfile = profileList.getSelectionModel().getSelectedItem();
    KafkaConfig kafkaConfig = db.fetch(new KafkaConfig(selectedProfile));

    KafkaPilot.getKafkaPilotController().switchToKafkaManagementView(kafkaConfig);
  }
}
