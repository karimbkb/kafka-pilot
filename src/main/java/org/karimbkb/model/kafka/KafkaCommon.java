package org.karimbkb.model.kafka;

import org.karimbkb.entity.KafkaConfig;

import java.sql.SQLException;

public interface KafkaCommon {
    KafkaConfig getCurrentKafkaConfig() throws SQLException;
}
