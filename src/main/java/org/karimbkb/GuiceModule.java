package org.karimbkb;

import com.google.inject.AbstractModule;
import org.karimbkb.dao.Database;
import org.karimbkb.dao.SQLite;
import org.karimbkb.model.KafkaManagement;
import org.karimbkb.model.KafkaManagementModel;

public class GuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Database.class).to(SQLite.class);
    bind(KafkaManagement.class).to(KafkaManagementModel.class);
  }
}
