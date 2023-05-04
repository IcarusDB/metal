package org.metal.server.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SpecJson {

  public static JsonObject empty() {
    return new JsonObject()
        .put("version", "1.0")
        .put("metals", new JsonArray())
        .put("edges", new JsonArray());
  }

  public static boolean check(JsonObject spec) throws IllegalArgumentException {
    String version = spec.getString("version");

    if (version == null || version.isBlank()) {
      throw new IllegalArgumentException("Fail to found version in spec.");
    }

    try {
      JsonArray metals = spec.getJsonArray("metals");
      if (metals == null) {
        throw new IllegalArgumentException("Fail to found metals in spec.");
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    try {
      JsonArray edges = spec.getJsonArray("edges");
      if (edges == null) {
        throw new IllegalArgumentException("Fail to found edges in spec.");
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return true;
  }
}
