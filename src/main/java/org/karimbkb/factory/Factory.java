package org.karimbkb.factory;

import com.google.inject.Inject;
import org.karimbkb.model.SchemaHandler;
import org.karimbkb.model.kafka.Common;
import org.karimbkb.model.kafka.Consumer;
import org.karimbkb.model.kafka.Reader;
import org.karimbkb.model.kafka.avro.AvroReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class Factory implements ConsumerFactory {
  private final SchemaHandler schemaHandler;
  private final Common common;

  @Inject
  public Factory(SchemaHandler schemaHandler, Common common) {
    this.schemaHandler = schemaHandler;
    this.common = common;
  }

  public Consumer get(String topic)
      throws SQLException, IOException, InterruptedException {
    if (schemaHandler.getAllVersions(topic) != null) {
      return new AvroReader(common);
    } else {
      return new Reader(common);
    }
  }
}
