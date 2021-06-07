package org.karimbkb.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.KafkaPilot;
import org.karimbkb.model.Notification;
import org.karimbkb.model.kafka.Consumer;
import org.karimbkb.model.kafka.Producer;
import org.karimbkb.model.kafka.avro.AvroProducer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProduceMessageController implements Initializable {

  private final FileChooser fileChooser = new FileChooser();
  private final Producer producer;
  private final Consumer consumer;
  private final AvroProducer avroProducer;
  private KafkaManagementController kafkaManagementController;

  @FXML private ComboBox<String> topicComboBox;
  @FXML private TextArea messageTextArea;
  @FXML private Button openFilePickerBtn;
  @FXML private Button submitMessageBtn;
  @FXML private Text infoText;
  @FXML private TextField filePathTextField;
  @FXML private CheckBox produceAvroCheckbox;

  @Inject
  public ProduceMessageController(
      Producer producer, Consumer consumer, AvroProducer avroProducer) {
    this.producer = producer;
    this.consumer = consumer;
    this.avroProducer = avroProducer;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Map<String, List<PartitionInfo>> topics;
    try {
      topics = consumer.loadKafkaTopics();
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Loading topics failed", e).showAndWait();
      return;
    }

    topicComboBox.getItems().addAll(new ArrayList<>(topics.keySet()));
    topicComboBox
        .getSelectionModel()
        .select(KafkaPilot.registry.getData(KafkaManagementController.CURRENT_TOPIC));
  }

  @FXML
  private void submitMessage() {
    try {
      submitMessageBtn.setDisable(true);
      if (produceAvroCheckbox.isSelected()) {
        String schemaFile = avroProducer.readSchemaFile(filePathTextField.getText());
        avroProducer.produceAvroMessage(
            topicComboBox.getSelectionModel().getSelectedItem(),
            messageTextArea.getText(),
            schemaFile);
      } else {
        producer.produceMessage(
            topicComboBox.getSelectionModel().getSelectedItem(), messageTextArea.getText());
      }
      infoText.setText("Message successfully produced.");
      kafkaManagementController.loadKafkaMessages(topicComboBox.getSelectionModel().getSelectedItem());
    } catch (FileNotFoundException e) {
      Notification.createExceptionAlert("Error", "Schema file was not found", e).showAndWait();
    } catch (Exception e) {
      Notification.createExceptionAlert("Error", "Submitting message failed", e).showAndWait();
    }
    submitMessageBtn.setDisable(false);
  }

  @FXML
  private void removeMessage() {
    infoText.setText("");
  }

  @FXML
  private void openFilePicker() {
    fileChooser.setTitle("Choose Kafka Avro File");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("avsc", "*.avsc"),
            new FileChooser.ExtensionFilter("avro", "*.avro"));

    Stage stage = (Stage) openFilePickerBtn.getScene().getWindow();
    File file = fileChooser.showOpenDialog(stage);
    if (file != null) {
      openFile(file);
    }
  }

  @FXML
  private void setFieldStatus() {
    if (produceAvroCheckbox.isSelected()) {
      openFilePickerBtn.setDisable(false);
      filePathTextField.setDisable(false);
    } else {
      openFilePickerBtn.setDisable(true);
      filePathTextField.setDisable(true);
    }
  }

  public void setKafkaManagementController(KafkaManagementController kafkaManagementController) {
    this.kafkaManagementController = kafkaManagementController;
  }

  private void openFile(File file) {
    filePathTextField.setText(file.getAbsolutePath());
  }
}
