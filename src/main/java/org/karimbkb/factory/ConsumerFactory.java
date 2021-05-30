package org.karimbkb.factory;

import org.karimbkb.model.kafka.Consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public interface ConsumerFactory {
    public Consumer get(String selectedTopic)
            throws SQLException, IOException, URISyntaxException, InterruptedException;
}
