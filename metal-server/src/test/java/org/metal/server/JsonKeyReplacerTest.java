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

import org.metal.server.util.JsonKeyReplacer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonKeyReplacerTest {

    @Test
    public void test0() {
        JsonObject json = new JsonObject();
        json.put("spark.o.version", "1.0");
        json.put(
                "conf",
                new JsonObject()
                        .put(
                                "spark.k.v",
                                new JsonArray()
                                        .add(new JsonArray().add(1))
                                        .add(
                                                new JsonObject()
                                                        .put("spark.k.v", 2)
                                                        .put("spark.k1.v1", 4))));
        System.out.println(json.encodePrettily());
        System.out.println(JsonKeyReplacer.compatBson(json).encodePrettily());
        System.out.println(
                JsonKeyReplacer.compatJson(JsonKeyReplacer.compatBson(json)).encodePrettily());
    }
}
