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
import org.karimbkb.factory.ConsumerFactory;
import org.karimbkb.model.Notification;
import org.karimbkb.model.Util;
import org.karimbkb.model.kafka.Consumer;
import org.karimbkb.model.kafka.Producer;
import org.karimbkb.model.kafka.avro.AvroProducer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class KafkaManagementController implements Initializable {

  public static final String MAX_DISPLAY_MESSAGES = "max_display_messages";
  public static final String CURRENT_TOPIC = "current_topic";

  private final Producer producer;
  private Consumer consumer;
  private final AvroProducer avroProducer;
  private final ConsumerFactory consumerFactory;
  private final Util util;
  private final ThreadPoolExecutor executor =
      (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  @FXML private ListView<String> topicsListView;
  @FXML private TableView<KafkaMessage> messagesTableView;
  @FXML private Button produceMessageBtn;
  @FXML private Text statusText;
  @FXML private Text topicText;
  @FXML private TextField numberOfMessages;
  @FXML private ProgressIndicator progressBar;

  @Inject
  public KafkaManagementController(
      Producer producer,
      Consumer consumer,
      AvroProducer avroProducer,
      ConsumerFactory consumerFactory,
      Util util) {
    this.producer = producer;
    this.consumer = consumer;
    this.avroProducer = avroProducer;
    this.consumerFactory = consumerFactory;
    this.util = util;
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
            statusText.setText("Loading messages...0");
            messagesTableView.getItems().clear();

            executor.submit(
                () -> {
                  loadKafkaMessages(selectedTopic);
                });
          }
        });
  }

  public void loadKafkaMessages(String selectedTopic) {
    List<KafkaMessage> kafkaMessages;
    try {
      consumer = consumerFactory.get(selectedTopic);
      kafkaMessages = consumer.loadMessagesByTopic(selectedTopic);

      messagesTableView.getItems().clear();
      kafkaMessages.forEach(
          message -> {
            progressBar.setProgress(
                (float) messagesTableView.getItems().size() / kafkaMessages.size());
            statusText.setText("Loading messages..." + (messagesTableView.getItems().size() + 1));
            messagesTableView.getItems().add(message);
          });
      topicsListView.setDisable(false);
      progressBar.setProgress(0);
    } catch (IOException e) {
      Notification.createExceptionAlert("Error", "Could not read the file.", e).showAndWait();
    } catch (InterruptedException e) {
      Notification.createExceptionAlert("Error", "Thread issue led to exception.", e).showAndWait();
    } catch (SQLException e) {
      Notification.createExceptionAlert("Error", "Could not read the database.", e).showAndWait();
    } catch (URISyntaxException e) {
      Notification.createExceptionAlert("Error", "Schema url was wrong.", e).showAndWait();
    }
  }

  @FXML
  private void openProduceMessageWindow() throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader();
    Injector injector = getInjector();
    fxmlLoader.setControllerFactory(injector::getInstance);
    Parent produceMessageRoot =
        fxmlLoader.load(getClass().getResourceAsStream("/fxml/ProduceMessageScene.fxml"));
    ProduceMessageController produceMessageController = fxmlLoader.getController();
    produceMessageController.setKafkaManagementController(this);

    Stage stage = new Stage();
    stage.setScene(new Scene(produceMessageRoot));
    stage.show();
  }

  @FXML
  private void openCreateTopicWindow() throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader();
    Injector injector = getInjector();
    fxmlLoader.setControllerFactory(injector::getInstance);
    Parent createTopicRoot =
        fxmlLoader.load(getClass().getResourceAsStream("/fxml/CreateTopicScene.fxml"));
    CreateTopicController createTopicController = fxmlLoader.getController();
    createTopicController.setKafkaManagementController(this);

    Stage stage = new Stage();
    stage.setScene(new Scene(createTopicRoot));
    stage.show();
  }

  @FXML
  private void deleteTopic() {
    String selectedTopic = topicsListView.getSelectionModel().getSelectedItem();

    if (selectedTopic == null) {
      Notification.createAlert("Warning", "Topic missing", "No topic to delete selected.")
          .showAndWait();
      return;
    }

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

  public void loadKafkaTopics() {
    executor.submit(
        () ->
            Platform.runLater(
                () -> {
                  Map<String, List<PartitionInfo>> topics;
                  try {
                    topics = consumer.loadKafkaTopics();
                    topicsListView.getItems().clear();
                    topicsListView.getItems().addAll(new ArrayList<>(topics.keySet()));
                  } catch (Exception e) {
                    Notification.createExceptionAlert("Error", "Loading Kafka Topics failed", e)
                        .showAndWait();
                  }
                }));
  }
}
