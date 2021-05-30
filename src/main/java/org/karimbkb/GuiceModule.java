package org.karimbkb;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.karimbkb.dao.Database;
import org.karimbkb.dao.SQLite;
import org.karimbkb.factory.ConsumerFactory;
import org.karimbkb.factory.Factory;
import org.karimbkb.model.Data;
import org.karimbkb.model.Registry;
import org.karimbkb.model.SchemaHandler;
import org.karimbkb.model.SchemaRegistry;
import org.karimbkb.model.kafka.*;
import org.karimbkb.model.kafka.avro.AvroProducer;
import org.karimbkb.model.kafka.avro.AvroWriter;

public class GuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Database.class).to(SQLite.class);
    bind(Registry.class).to(Data.class).in(Singleton.class);
    bind(SchemaHandler.class).to(SchemaRegistry.class);
    bind(Consumer.class).to(Reader.class);
    bind(Producer.class).to(Writer.class);
    bind(AvroProducer.class).to(AvroWriter.class);
    //bind(Consumer.class).to(AvroReader.class);
    bind(KafkaCommon.class).to(Common.class);
    bind(ConsumerFactory.class).to(Factory.class);
  }
}
