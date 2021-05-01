package org.karimbkb.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class KafkaConfig {
  @NonNull private final String profileName;
  @NonNull private String bootstrapServer;
  @NonNull private String groupId;

  public KafkaConfig(@NonNull String profileName) {
    this.profileName = profileName;
  }
}
