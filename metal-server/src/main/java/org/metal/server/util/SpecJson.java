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
