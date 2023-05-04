package org.metal.server.util;

import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.List;

public class JsonConvertor {

  public static List<String> jsonArrayToList(JsonArray array) {
    List<String> ret = new ArrayList<>();
    if (array == null) {
      return ret;
    }

    if (array != null) {
      for (int idx = 0; idx < array.size(); idx++) {
        String e = array.getString(idx);
        if (e != null && !e.isBlank()) {
          ret.add(e);
        }
      }
    }
    return ret;
  }
}
