package org.karimbkb.controller;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.karimbkb.GuiceModule;
import org.karimbkb.entity.KafkaConfig;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KafkaPilotController implements Initializable {

  @FXML private TabPane mainTabPane;

  @Override
  public void initialize(URL url, ResourceBundle rb) {}

  @FXML
  private void addNewTab() throws IOException {
    if (mainTabPane.getSelectionModel().getSelectedIndex() == 0) {
      Injector injector = Guice.createInjector(new GuiceModule());
      FXMLLoader fxmlLoader = new FXMLLoader();

      fxmlLoader.setControllerFactory(injector::getInstance);

      Node node = fxmlLoader.load(getClass().getResourceAsStream("/fxml/KafkaConfigScene.fxml"));
      mainTabPane.getTabs().add(new Tab("Tab " + mainTabPane.getTabs().size(), node));
      mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1);
    }
  }

  public void switchToKafkaManagementView(KafkaConfig kafkaConfig) throws IOException {
    Injector injector = Guice.createInjector(new GuiceModule());
    FXMLLoader fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(injector::getInstance);

    mainTabPane.getSelectionModel().getSelectedItem().setText(kafkaConfig.getProfileName());
    Node node = fxmlLoader.load(getClass().getResourceAsStream("/fxml/KafkaManagementScene.fxml"));
    mainTabPane.getSelectionModel().getSelectedItem().setContent(node);
  }

  public Tab getCurrentSelectedItem() {
    return mainTabPane.getSelectionModel().getSelectedItem();
  }
}
