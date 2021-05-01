package org.karimbkb;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.karimbkb.controller.KafkaPilotController;

public class KafkaPilot extends Application {

  private static KafkaPilotController kafkaPilotController = null;

  public static KafkaPilotController getKafkaPilotController() {
    if (kafkaPilotController == null) {
      return new KafkaPilotController();
    }
    return kafkaPilotController;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Injector injector = Guice.createInjector(new GuiceModule());
    FXMLLoader fxmlLoader = new FXMLLoader();

    fxmlLoader.setControllerFactory(injector::getInstance);

    Parent root = fxmlLoader.load(getClass().getResourceAsStream("/fxml/KafkaPilotScene.fxml"));
    kafkaPilotController = fxmlLoader.getController();
    Scene scene = new Scene(root);

    stage.setTitle("Kafka Pilot");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
