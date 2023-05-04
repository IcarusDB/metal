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

package org.metal.server.user;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

public class UserDB {

  public final static String DB = "user";
  public final static String FIELD_ID = "_id";
  public final static String FIELD_USER_NAME = "username";
  public final static String FIELD_PASSWORD = "password";
  public final static String FIELD_ROLES = "roles";

  public static Future<Void> createCollection(MongoClient mongo) {
    return mongo.createCollection(DB)
        .compose(ret -> {
          return mongo.createIndexWithOptions(
              DB,
              new JsonObject().put(FIELD_USER_NAME, true),
              new IndexOptions().unique(true)
          );
        });
  }

  public static Future<JsonObject> get(MongoClient mongo, String username) {
    return mongo.findOne(
        DB,
        new JsonObject().put(FIELD_USER_NAME, username),
        new JsonObject());
  }

  public static Future<JsonObject> getWithoutPassword(MongoClient mongo, String username) {
    return mongo.findOne(
        DB,
        new JsonObject().put(FIELD_USER_NAME, username),
        new JsonObject()
            .put(FIELD_ID, true)
            .put(FIELD_USER_NAME, true)
            .put(FIELD_ROLES, true)
    );
  }

  public static Future<JsonObject> update(MongoClient mongo, JsonObject matcher,
      JsonObject updater) {
    return mongo.updateCollection(DB, matcher, updater)
        .compose(result -> {
          if (result.getDocMatched() == 0) {
            return Future.failedFuture(
                String.format("Fail to match any record. Matcher: %s", matcher.toString()));
          }
          return Future.succeededFuture(result.toJson());
        });
  }
}
