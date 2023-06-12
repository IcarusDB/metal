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
