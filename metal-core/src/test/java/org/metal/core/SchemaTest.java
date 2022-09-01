package org.metal.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.junit.Test;

public class SchemaTest {
  @Test
  public void case0() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper);
    JsonSchema schema = generator.generateSchema(Mock.MSourceImpl.class);
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
        schema
    ));
  }
}
