/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.core;

import com.fasterxml.jackson.core.JsonProcessingException;
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
