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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

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
            formSchema =
                    mapper.readTree(mapper.writer().writeValueAsString(FormJsonSchema.of(clz)));
            schema.putIfAbsent("formSchema", formSchema);
            return schema.toPrettyString();
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
