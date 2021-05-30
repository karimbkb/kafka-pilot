package org.karimbkb.dao;

import org.karimbkb.entity.KafkaConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLite implements Database {

  @Override
  public void createTable() throws SQLException {
    String sql =
        "CREATE TABLE IF NOT EXISTS config"
            + " (profile_name           VARCHAR(128)   NOT NULL,"
            + " bootstrap_server            VARCHAR(255)    NOT NULL,"
            + " schema_registry_url        VARCHAR(255)    NOT NULL,"
            + " group_id        VARCHAR(64) NOT NULL,"
            + " PRIMARY KEY (profile_name));";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.executeUpdate();
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:src/test.db");
  }

  @Override
  public void insert(KafkaConfig kafkaConfig) throws SQLException {
    String sql =
        "INSERT INTO config (profile_name, bootstrap_server, schema_registry_url, group_id) VALUES (?, ?, ?, ?);";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.setString(2, kafkaConfig.getBootstrapServer());
      pstmt.setString(3, kafkaConfig.getSchemaRegistryUrl());
      pstmt.setString(4, kafkaConfig.getGroupId());

      pstmt.executeUpdate();
    }
  }

  @Override
  public KafkaConfig fetch(KafkaConfig kafkaConfig) throws SQLException {
    String sql = "SELECT * FROM config WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());

      ResultSet rs = pstmt.executeQuery();
      return new KafkaConfig(
          rs.getString("profile_name"),
          rs.getString("bootstrap_server"),
          rs.getString("schema_registry_url"),
          rs.getString("group_id"));
    }
  }

  @Override
  public List<KafkaConfig> fetchAll() throws SQLException {
    String sql = "SELECT * FROM config;";
    List<KafkaConfig> kafkaConfigList = new ArrayList<>();

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        kafkaConfigList.add(
            new KafkaConfig(
                rs.getString("profile_name"),
                rs.getString("bootstrap_server"),
                rs.getString("schema_registry_url"),
                rs.getString("group_id")));
      }
      rs.close();

      return kafkaConfigList;
    }
  }

  @Override
  public void update(KafkaConfig kafkaConfig) throws SQLException {
    String sql =
        "UPDATE config set profile_name = ? , bootstrap_server = ?, schema_registry_url = ?, group_id = ? WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.setString(2, kafkaConfig.getBootstrapServer());
      pstmt.setString(3, kafkaConfig.getSchemaRegistryUrl());
      pstmt.setString(4, kafkaConfig.getGroupId());
      pstmt.setString(5, kafkaConfig.getProfileName());
      pstmt.executeUpdate();
    }
  }

  @Override
  public void delete(KafkaConfig kafkaConfig) throws SQLException {
    String sql = "DELETE FROM config WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.executeQuery();
    }
  }
}
