package org.karimbkb.model;

import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class Data implements Registry {

  private final Map<String, String> registry = new HashMap<>();

  @Override
  public void setData(String key, String value) {
    registry.put(key, value);
  }

  @Override
  public String getData(String key) {
    return registry.get(key);
  }
}
