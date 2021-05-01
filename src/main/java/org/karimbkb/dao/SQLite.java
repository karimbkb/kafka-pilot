package org.karimbkb.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.karimbkb.entity.KafkaConfig;

public class SQLite implements Database {

  @Override
  public void createTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS config"
            + " (profile_name           VARCHAR(128)   NOT NULL,"
            + " bootstrap_server            VARCHAR(255)    NOT NULL,"
            + " group_id        VARCHAR(64) NOT NULL,"
            + " PRIMARY KEY (profile_name));";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.executeUpdate();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:src/test.db");
  }

  @Override
  public void insert(KafkaConfig kafkaConfig) {
    String sql = "INSERT INTO config (profile_name, bootstrap_server, group_id) VALUES (?, ?, ?);";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.setString(2, kafkaConfig.getBootstrapServer());
      pstmt.setString(3, kafkaConfig.getGroupId());

      pstmt.executeUpdate();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  @Override
  public KafkaConfig fetch(KafkaConfig kafkaConfig) {
    String sql = "SELECT * FROM config WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());

      ResultSet rs = pstmt.executeQuery();
      return new KafkaConfig(
          rs.getString("profile_name"), rs.getString("bootstrap_server"), rs.getString("group_id"));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    return null;
  }

  @Override
  public List<KafkaConfig> fetchAll() {
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
                rs.getString("group_id")));
      }
      rs.close();

      return kafkaConfigList;
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    return null;
  }

  @Override
  public void update(KafkaConfig kafkaConfig) {
    String sql =
        "UPDATE config set profile_name = ? , bootstrap_server = ?, group_id = ? WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.setString(2, kafkaConfig.getBootstrapServer());
      pstmt.setString(3, kafkaConfig.getGroupId());
      pstmt.setString(4, kafkaConfig.getProfileName());
      pstmt.executeUpdate();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  @Override
  public void delete(KafkaConfig kafkaConfig) {
    String sql = "DELETE FROM config WHERE profile_name = ?;";

    try (Connection c = getConnection();
        PreparedStatement pstmt = c.prepareStatement(sql)) {

      pstmt.setString(1, kafkaConfig.getProfileName());
      pstmt.executeQuery();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
