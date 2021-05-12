package org.karimbkb.controller;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.PartitionInfo;
import org.karimbkb.GuiceModule;
import org.karimbkb.KafkaPilot;
import org.karimbkb.dto.KafkaMessage;
import org.karimbkb.model.KafkaManagement;
import org.karimbkb.model.Notification;
import org.karimbkb.model.Registry;
import org.karimbkb.model.Util;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class KafkaManagementController implements Initializable {

  public static final String MAX_DISPLAY_MESSAGES = "max_display_messages";
  public static final String CURRENT_TOPIC = "current_topic";

  private final KafkaManagement kafkaManagement;
  private final Util util;
  private final ThreadPoolExecutor executor =
      (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public static FXMLLoader fxmlLoader = new FXMLLoader();

  @FXML private ListView<String> topicsListView;
  @FXML private TableView<KafkaMessage> messagesTableView;
  @FXML private Button produceMessageBtn;
  @FXML private Text statusText;
  @FXML private Text topicText;
  @FXML private TextField numberOfMessages;
  @FXML private ProgressIndicator progressBar;

  @Inject
  public KafkaManagementController(KafkaManagement kafkaManagement, Registry registry, Util util) {
    this.kafkaManagement = kafkaManagement;
    this.util = util;

    Injector injector = getInjector();
    fxmlLoader.setControllerFactory(injector::getInstance);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initMaxMessagesToLoad();

    produceMessageBtn.setDisable(true);
    loadKafkaTopics();

    topicsListView.setOnMouseClicked(
        click -> {
          if (click.getClickCount() == 2) {
            topicsListView.setDisable(true);
            produceMessageBtn.setDisable(false);
            String selectedTopic = topicsListView.getSelectionModel().getSelectedItem();
            KafkaPilot.registry.setData(CURRENT_TOPIC, selectedTopic);
            topicText.setText("Selected topic: " + selectedTopic);
            messagesTableView.getItems().clear();

            executor.submit(
                () -> {
                  List<KafkaMessage> kafkaMessages =
                      kafkaManagement.loadMessagesByTopic(selectedTopic);
                  kafkaMessages.forEach(
                      message -> {
                        progressBar.setProgress(
                            (float) messagesTableView.getItems().size() / kafkaMessages.size());
                        statusText.setText(
                            "Loading messages..." + messagesTableView.getItems().size());
                        messagesTableView.getItems().add(message);
                      });
                  topicsListView.setDisable(false);
                  progressBar.setProgress(0);
                });
          }
        });
  }

  @FXML
  private void openProduceMessageWindow() throws IOException {

    Parent produceMessageRoot =
        fxmlLoader.load(getClass().getResourceAsStream("/fxml/ProduceMessageScene.fxml"));

    Stage stage = new Stage();
    stage.setScene(new Scene(produceMessageRoot));
    stage.show();
  }

  @FXML
  private void openCreateTopicWindow() throws IOException {

    Parent createTopicRoot =
        fxmlLoader.load(getClass().getResourceAsStream("/fxml/CreateTopicScene.fxml"));

    Stage stage = new Stage();
    stage.setScene(new Scene(createTopicRoot));
    stage.show();
  }

  @FXML
  private void deleteTopic() {
    String selectedTopic = topicsListView.getSelectionModel().getSelectedItem();

    Alert alert =
        Notification.createAlert(
            "Confirmation",
            "Please confirm",
            "Do you really want to delete the topic: " + selectedTopic);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Properties properties = util.getPropertiesForCurrentProfile();

      try (AdminClient adminClient = AdminClient.create(properties)) {
        adminClient.deleteTopics(Collections.singleton(selectedTopic));
        topicsListView.getItems().removeAll(selectedTopic);
      } catch (NumberFormatException e) {
        Notification.createExceptionAlert("Error", "Deleting topic failed", e).showAndWait();
      }
    }
  }

  @FXML
  private void reloadTopics() {
    loadKafkaTopics();
  }

  @FXML
  private void setMaxDisplayedMessage() {
    KafkaPilot.registry.setData(MAX_DISPLAY_MESSAGES, numberOfMessages.getText());
  }

  private Injector getInjector() {
    return Guice.createInjector(new GuiceModule());
  }

  private void initMaxMessagesToLoad() {
    numberOfMessages.setText("100");
    KafkaPilot.registry.setData(MAX_DISPLAY_MESSAGES, numberOfMessages.getText());
  }

  private void loadKafkaTopics() {
    executor.submit(
            () ->
                    Platform.runLater(
                            () -> {
                              Map<String, List<PartitionInfo>> topics = kafkaManagement.loadKafkaTopics();
                              topicsListView.getItems().clear();
                              topicsListView.getItems().addAll(new ArrayList<>(topics.keySet()));
                            }));
  }
}
