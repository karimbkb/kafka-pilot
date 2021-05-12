package org.karimbkb.dao;

import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;
import java.util.List;

public interface Database {
  void createTable() throws SQLException;

  void insert(KafkaConfig kafkaConfig) throws SQLException;

  KafkaConfig fetch(KafkaConfig kafkaConfig) throws SQLException;

  List<KafkaConfig> fetchAll() throws SQLException;

  void update(KafkaConfig kafkaConfig) throws SQLException;

  void delete(KafkaConfig kafkaConfig) throws SQLException;
}
