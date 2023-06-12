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

package org.metal.server.db;

import org.metal.server.auth.Roles;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

import static org.metal.server.auth.Auth.HASH_ALGO;

public class Init {

    private static final Logger LOGGER = LoggerFactory.getLogger(Init.class);

    public static Future<Void> initUser(MongoClient mongo) {
        MongoAuthenticationOptions options = new MongoAuthenticationOptions();
        MongoAuthentication authenticationProvider = MongoAuthentication.create(mongo, options);
        return mongo.createCollection("user")
                .compose(
                        ret -> {
                            return mongo.createIndexWithOptions(
                                    "user",
                                    new JsonObject().put("username", 1),
                                    new IndexOptions().unique(true));
                        })
                .compose(
                        ret -> {
                            String hash = authenticationProvider.hash(HASH_ALGO, "", "123456");
                            return mongo.insert(
                                    "user",
                                    new JsonObject()
                                            .put("username", "admin")
                                            .put("password", hash)
                                            .put(
                                                    "roles",
                                                    new JsonArray().add(Roles.ADMIN.toString())));
                        })
                .compose(
                        ret -> {
                            return Future.succeededFuture();
                        });
    }

    public static void initTask(MongoClient mongo) {}
}
