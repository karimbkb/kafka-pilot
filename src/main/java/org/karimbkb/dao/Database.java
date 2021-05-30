package org.karimbkb.dao;

import java.sql.SQLException;
import java.util.List;
import org.karimbkb.entity.KafkaConfig;

public interface Database {
  void createTable() throws SQLException;

  void insert(KafkaConfig kafkaConfig) throws SQLException;

  KafkaConfig fetch(KafkaConfig kafkaConfig) throws SQLException;

  List<KafkaConfig> fetchAll() throws SQLException;

  void update(KafkaConfig kafkaConfig) throws SQLException;

  void delete(KafkaConfig kafkaConfig) throws SQLException;
}
