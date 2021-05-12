package org.karimbkb.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.KafkaPilot;
import org.karimbkb.model.KafkaManagement;
import org.karimbkb.model.Notification;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProduceMessageController implements Initializable {

  private final KafkaManagement kafkaManagement;

  @FXML private ComboBox<String> topicComboBox;
  @FXML private TextArea messageTextArea;
  @FXML private Button submitMessageBtn;
  @FXML private Text infoText;

  @Inject
  public ProduceMessageController(KafkaManagement kafkaManagement) {
    this.kafkaManagement = kafkaManagement;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Map<String, List<PartitionInfo>> topics = kafkaManagement.loadKafkaTopics();
    topicComboBox.getItems().addAll(new ArrayList<>(topics.keySet()));
    topicComboBox.getSelectionModel().select(KafkaPilot.registry.getData(KafkaManagementController.CURRENT_TOPIC));
  }

  @FXML
  private void submitMessage() {
    try {
      submitMessageBtn.setDisable(true);
      kafkaManagement.produceMessage(
          topicComboBox.getSelectionModel().getSelectedItem(), messageTextArea.getText());
      infoText.setText("Message successfully produced.");
      submitMessageBtn.setDisable(false);
    } catch (Exception e) {
      Notification.createExceptionAlert("Error", "Submitting message failed", e).showAndWait();
    }
  }

  @FXML
  private void removeMessage() {
    infoText.setText("");
  }
}
