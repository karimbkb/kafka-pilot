package org.karimbkb.dao;

import org.karimbkb.entity.KafkaConfig;

import java.util.List;

public interface Database {
  void createTable();

  void insert(KafkaConfig kafkaConfig);

  KafkaConfig fetch(KafkaConfig kafkaConfig);

  List<KafkaConfig> fetchAll();

  void update(KafkaConfig kafkaConfig);

  void delete(KafkaConfig kafkaConfig);
}
