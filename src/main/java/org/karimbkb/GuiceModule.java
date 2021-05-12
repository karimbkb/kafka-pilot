package org.karimbkb;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.karimbkb.dao.Database;
import org.karimbkb.dao.SQLite;
import org.karimbkb.model.Data;
import org.karimbkb.model.KafkaManagement;
import org.karimbkb.model.KafkaManagementModel;
import org.karimbkb.model.Registry;

public class GuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Database.class).to(SQLite.class);
    bind(KafkaManagement.class).to(KafkaManagementModel.class);
    bind(Registry.class).to(Data.class).in(Singleton.class);
  }
}
