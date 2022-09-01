package org.metal.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import java.lang.annotation.Annotation;
import org.metal.core.props.IMetalProps;

public class FormJsonSchema {
  public static JsonSchema of(Class<?> formClz) {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper);
    try {
      JsonSchema schema = generator.generateSchema(formClz);
      return schema;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String formSchema(Class<?> clz) {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode schema = mapper.createObjectNode();
    JsonNode formSchema = null;
    try {
      formSchema = mapper.readTree(
          mapper.writer().writeValueAsString(
              FormJsonSchema.of(clz)
          )
      );
      schema.putIfAbsent("formSchema", formSchema);
      return schema.toPrettyString();
    } catch (JsonProcessingException e) {
      return null;
    }
  }
}
