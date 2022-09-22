package org.metal.server.user;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

public class UserDB {
  public final static String DB = "user";
  public final static String FIELD_ID = "_id";
  public final static String FIELD_USER_NAME = "username";

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
}
