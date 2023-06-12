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
            } else if (val instanceof JsonArray) {
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
        Function<String, String> replacer =
                (String fieldName) -> {
                    return fieldName.replaceAll("\\.", "__");
                };
        return compat(json, replacer);
    }

    public static JsonArray compatBson(JsonArray array) {
        Function<String, String> replacer =
                (String fieldName) -> {
                    return fieldName.replaceAll("\\.", "__");
                };
        return compat(array, replacer);
    }

    public static JsonObject compatJson(JsonObject json) {
        Function<String, String> replacer =
                (String fieldName) -> {
                    return fieldName.replaceAll("__", ".");
                };
        return compat(json, replacer);
    }

    public static JsonArray compatJson(JsonArray array) {
        Function<String, String> replacer =
                (String fieldName) -> {
                    return fieldName.replaceAll("__", ".");
                };
        return compat(array, replacer);
    }
}
