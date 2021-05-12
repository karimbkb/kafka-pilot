package org.karimbkb.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.karimbkb.model.KafkaManagement;
import org.karimbkb.model.Notification;
import org.karimbkb.model.Util;

import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.ResourceBundle;

public class CreateTopicController implements Initializable {

  private final KafkaManagement kafkaManagement;
  private final Util util;

  @FXML private TextField topicName;
  @FXML private TextField replicationFactor;
  @FXML private TextField numberOfPartitions;
  @FXML private Button saveTopicBtn;

  @Inject
  public CreateTopicController(KafkaManagement kafkaManagement, Util util) {
    this.kafkaManagement = kafkaManagement;
    this.util = util;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {}

  @FXML
  private void saveTopic() {
    Properties properties = util.getPropertiesForCurrentProfile();

    try (AdminClient adminClient = AdminClient.create(properties)) {
      NewTopic newTopic =
          new NewTopic(
              topicName.getText(),
              Integer.parseInt(numberOfPartitions.getText()),
              Short.parseShort(replicationFactor.getText()));

      adminClient.createTopics(Collections.singleton(newTopic));
      Stage stage = (Stage) saveTopicBtn.getScene().getWindow();
      stage.close();
    } catch (NumberFormatException e) {
      Notification.createExceptionAlert("Error", "Saving topic failed", e).showAndWait();
    }
  }
}
