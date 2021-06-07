package org.karimbkb.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface SchemaHandler {
  List<String> getAllVersions(String topic)
          throws IOException, InterruptedException, SQLException;

  void getSchemaByVersion(String topic, int version) throws IOException, InterruptedException, SQLException;
}
