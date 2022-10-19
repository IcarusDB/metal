package org.metal.server.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.function.Function;

public class JsonKeyReplacer {
  public static JsonObject compat(JsonObject json, Function<String, String> replacer) {
    JsonObject copy = new JsonObject();
    for (String fieldName : json.fieldNames()) {
      String newFieldName = replacer.apply(fieldName);
      Object val = json.getValue(fieldName);
      Object newVal = val;
      if (val instanceof JsonObject) {
        newVal = compat((JsonObject) val, replacer);
      } else if (val instanceof JsonArray){
        newVal = compat((JsonArray) val, replacer);
      }
      copy.put(newFieldName, newVal);
    }
    return copy;
  }

  public static JsonArray compat(JsonArray array, Function<String, String> replacer) {
    JsonArray copy = new JsonArray();
    for (Object obj : array) {
      Object newObj = obj;
      if (obj instanceof JsonObject) {
        newObj = compat((JsonObject) obj, replacer);
      } else if (obj instanceof JsonArray) {
        newObj = compat((JsonArray) obj, replacer);
      }
      copy.add(newObj);
    }
    return copy;
  }

  public static JsonObject compatBson(JsonObject json) {
    Function<String, String> replacer = (String fieldName)->{
      return fieldName.replaceAll("\\.", "__");
    };
    return compat(json, replacer);
  }

  public static JsonArray compatBson(JsonArray array) {
    Function<String, String> replacer = (String fieldName)->{
      return fieldName.replaceAll("\\.", "__");
    };
    return compat(array, replacer);
  }

  public static JsonObject compatJson(JsonObject json) {
    Function<String, String> replacer = (String fieldName)->{
      return fieldName.replaceAll("__", ".");
    };
    return compat(json, replacer);
  }

  public static JsonArray compatJson(JsonArray array) {
    Function<String, String> replacer = (String fieldName)->{
      return fieldName.replaceAll("__", ".");
    };
    return compat(array, replacer);
  }
}
