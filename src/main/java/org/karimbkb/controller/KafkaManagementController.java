package org.karimbkb.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.model.KafkaManagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class KafkaManagementController implements Initializable {

  private final KafkaManagement kafkaManagement;

  @FXML private ListView<String> topicsListView;
  @FXML private TableView<KafkaMessage> messagesTableView;

  @Inject
  public KafkaManagementController(KafkaManagement kafkaManagement) {
    this.kafkaManagement = kafkaManagement;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Map<String, List<PartitionInfo>> topics = kafkaManagement.loadKafkaTopics();
    topicsListView.getItems().addAll(new ArrayList<>(topics.keySet()));

    topicsListView.setOnMouseClicked(
        click -> {
          if (click.getClickCount() == 2) {
            String selectedTopic = topicsListView.getSelectionModel().getSelectedItem();
            this.kafkaManagement.loadMessagesByTopic(selectedTopic).forEach(message -> messagesTableView.getItems().add(message));
          }
        });
  }
}
