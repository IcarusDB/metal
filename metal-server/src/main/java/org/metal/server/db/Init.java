package org.metal.server.db;

import static org.metal.server.auth.Auth.HASH_ALGO;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import org.metal.server.auth.Roles;

public class Init {

  private final static Logger LOGGER = LoggerFactory.getLogger(Init.class);

  public static Future<Void> initUser(MongoClient mongo) {
    MongoAuthenticationOptions options = new MongoAuthenticationOptions();
    MongoAuthentication authenticationProvider = MongoAuthentication.create(mongo, options);
    return mongo.createCollection("user")
        .compose(ret -> {
          return mongo.createIndexWithOptions(
              "user",
              new JsonObject().put("username", 1),
              new IndexOptions().unique(true)
          );
        })
        .compose(ret -> {
          String hash = authenticationProvider.hash(HASH_ALGO, "", "123456");
          return mongo.insert("user",
              new JsonObject()
                  .put("username", "admin").put("password", hash)
                  .put("roles", new JsonArray().add(Roles.ADMIN.toString()))
          );
        })
        .compose(ret -> {
          return Future.succeededFuture();
        });
  }

  public static void initTask(MongoClient mongo) {

  }

}
