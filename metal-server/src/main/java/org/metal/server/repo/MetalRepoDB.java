package org.metal.server.repo;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import java.util.Optional;

public class MetalRepoDB {
  public static enum MetalType {
    SOURCE, MAPPER, FUSION, SINK, SETUP
  }

  public static enum MetalScope {
    PUBLIC, PRIVATE
  }

  public final static String DB = "metals";

  private static JsonObject parsePkg(String pkg) throws IllegalArgumentException{
    String[] s = pkg.strip().split(":");
    if (s.length != 3) {
      throw new IllegalArgumentException("Fail to parse groupId:artifactId:version from " + pkg);
    }

    return new JsonObject().put("groupId", s[0])
        .put("artifactId", s[1])
        .put("version", s[2]);
  }

  public static Future<String> add(
      MongoClient mongo, String userId,
      MetalType type,
      MetalScope scope,
      String pkg, String clazz,
      JsonObject formSchema, Optional<JsonObject> uiSchema
      ) {

    JsonObject metal = new JsonObject()
        .put("userId", userId)
        .put("type", type.toString())
        .put("scope", scope.toString())
        .put("createTime", System.currentTimeMillis())
        .put("pkg", pkg)
        .put("class", clazz)
        .put("formSchema", formSchema);

    try {
      JsonObject pkgInfo = parsePkg(pkg);
      metal.mergeIn(pkgInfo);
    } catch (IllegalArgumentException e) {
    }

    uiSchema.ifPresent(u -> {
      metal.put("uiSchema", u);
    });

    return mongo.insert(DB, metal);
  }

  public static Future<JsonObject> get(MongoClient mongo, String userId, String metalId) {
    return mongo.findOne(
        DB,
        new JsonObject().put("_id", metalId).put("userId", userId),
        new JsonObject());
  }

  public static ReadStream<JsonObject> getAllOfUser(MongoClient mongo, String userId) {
    return mongo.findBatch(
        DB,
        new JsonObject().put("userId", userId)
    );
  }

  public static ReadStream<JsonObject> getAllOfUserPrivate(MongoClient mongo, String userId) {
    return mongo.findBatch(
        DB,
        new JsonObject().put("userId", userId).put("scope", MetalScope.PRIVATE.toString())
    );
  }

  public static ReadStream<JsonObject> getAllOfPublic(MongoClient mongo) {
    return mongo.findBatch(
        DB,
        new JsonObject().put("scope", MetalScope.PUBLIC.toString())
    );
  }



  public static Future<MongoClientDeleteResult> removePrivate(MongoClient mongo, String userId, String metalId) {
    return mongo.removeDocument(
        DB,
        new JsonObject()
            .put("scope", MetalScope.PRIVATE.toString())
            .put("_id", metalId)
            .put("userId", userId)
    );
  }

  public static Future<MongoClientDeleteResult> removeAllPrivateOfUser(MongoClient mongo, String userId) {
    return mongo.removeDocuments(
        DB,
        new JsonObject()
            .put("scope", MetalScope.PRIVATE.toString())
            .put("userId", userId)
    );
  }
}
