package org.metal.backend.spark.extension;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class TableAliasDeSer extends JsonDeserializer<Map<String, String>> {

  @Override
  public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    JsonNode root = p.readValueAsTree();
    if (root.isObject()) {
      for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
        String field = it.next();
        JsonNode val = root.get(field);
        if (val.isValueNode()) {
          String value = val.textValue();
          builder.put(field, value);
        }
      }
    }
    return builder.build();
  }
}
