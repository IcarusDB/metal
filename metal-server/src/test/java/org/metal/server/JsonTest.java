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

package org.metal.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public class JsonTest {

    @Test
    public void test() {
        List<JsonObject> arr = List.of();
        Map<String, JsonObject> map = Map.of("k1", new JsonObject());
        System.out.println(JsonObject.mapFrom(map).toString());

        System.out.println(JsonArray.of(arr.toArray()));
    }
}
