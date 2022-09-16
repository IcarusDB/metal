package org.metal.server.user;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

public class UserDB {
  public final static String DB = "user";

  public static Future<Void> createCollection(MongoClient mongo) {
    return mongo.createCollection(DB)
        .compose(ret -> {
          return mongo.createIndexWithOptions(
              DB,
              new JsonObject().put("username", true),
              new IndexOptions().unique(true)
              );
        });
  }

  public static Future<JsonObject> get(MongoClient mongo, String username) {
    return mongo.findOne(
        DB,
        new JsonObject().put("username", username),
        new JsonObject());
  }
}
