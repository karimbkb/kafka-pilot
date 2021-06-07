package org.karimbkb.model;

public interface Registry {
  void setData(String key, String value);
  String getData(String key);
}
