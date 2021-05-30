package org.karimbkb.model;

import com.google.inject.Inject;
import org.json.JSONObject;
import org.karimbkb.entity.KafkaConfig;
import org.karimbkb.model.kafka.KafkaCommon;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;

public class SchemaRegistry implements SchemaHandler {

  private final KafkaCommon common;

  @Inject
  public SchemaRegistry(KafkaCommon common) {
    this.common = common;
  }

  @Override
  public List<String> getAllVersions(String topic)
      throws IOException, InterruptedException, SQLException {
    KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();
    HttpClient httpClient = HttpClient.newHttpClient();

    final HttpRequest request =
        HttpRequest.newBuilder(URI.create(kafkaConfig.getSchemaRegistryUrl() + getVersionsPath(topic)))
            .header("accept", "application/json")
            .build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      return null;
    }

    return List.of(response.body().replaceAll("\\[|\\]", "").split(","));
  }

  @Override
  public void getSchemaByVersion(int version) throws IOException, InterruptedException {
    HttpClient httpClient = HttpClient.newHttpClient();

    final HttpRequest request =
        HttpRequest.newBuilder(URI.create("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY"))
            .header("accept", "application/json")
            .build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JSONObject jsonObject = new JSONObject(response.body());
  }

  private String getVersionsPath(String currentTopic) {
    return "/subjects/" + currentTopic + "-value/versions";
  }

  private String getSchemaPath(String currentTopic, int version) {
    return "/subjects/" + currentTopic + "-value/versions/" + version;
  }
}
