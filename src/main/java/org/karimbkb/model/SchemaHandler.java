package org.karimbkb.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

public interface SchemaHandler {
  List<String> getAllVersions(String topic)
          throws IOException, InterruptedException, SQLException, URISyntaxException;

  void getSchemaByVersion(int version) throws IOException, InterruptedException;
}
