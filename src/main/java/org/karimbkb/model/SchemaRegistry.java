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

  public static final String VALUE_VERSIONS_PATH = "-value/versions/";
  public static final String SUBJECTS_PATH = "/subjects/";
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
        HttpRequest.newBuilder(
                URI.create(kafkaConfig.getSchemaRegistryUrl() + getVersionsPath(topic)))
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
  // TODO: define method
  public void getSchemaByVersion(String topic, int version) throws IOException, InterruptedException, SQLException {
    HttpClient httpClient = HttpClient.newHttpClient();
    KafkaConfig kafkaConfig = common.getCurrentKafkaConfig();

    final HttpRequest request =
        HttpRequest.newBuilder(
                URI.create(kafkaConfig.getSchemaRegistryUrl() + getVersionsPath(topic)))
            .header("accept", "application/json")
            .build();

    final HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JSONObject jsonObject = new JSONObject(response.body());
  }

  private String getVersionsPath(String currentTopic) {
    return SUBJECTS_PATH + currentTopic + VALUE_VERSIONS_PATH;
  }

  private String getSchemaPath(String currentTopic, int version) {
    return SUBJECTS_PATH + currentTopic + VALUE_VERSIONS_PATH + version;
  }
}
