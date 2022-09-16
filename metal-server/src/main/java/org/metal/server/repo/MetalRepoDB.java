package org.metal.server.repo;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import java.util.ArrayList;
import java.util.List;
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

  private static JsonObject metalOf(String userId,
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
    return metal;
  }

  public static Future<String> add(
      MongoClient mongo, String userId,
      MetalType type,
      MetalScope scope,
      String pkg, String clazz,
      JsonObject formSchema, Optional<JsonObject> uiSchema
      ) {
    JsonObject metal = metalOf(userId, type, scope, pkg, clazz, formSchema, uiSchema);
    return mongo.insert(DB, metal);
  }

  private static BulkOperation wrapBulkOperation(JsonObject metalRaw, String userId, MetalType type) {
    MetalScope scope = MetalScope.valueOf(metalRaw.getString("scope", "PUBLIC"));
    JsonObject metal = metalOf(
        userId, type, scope,
        metalRaw.getString("pkg"),
        metalRaw.getString("clazz"),
        metalRaw.getJsonObject("formSchema"),
        Optional.ofNullable(metalRaw.getJsonObject("uiSchema"))
    );
    return BulkOperation.createInsert(metal);
  }

  public static Future<MongoClientBulkWriteResult> addFromManifest(
      MongoClient mongo,
      String userId,
      JsonObject manifest
  ) {
    List<BulkOperation> operations = new ArrayList<>();
    JsonArray sources = manifest.getJsonArray("sources");
    sources.stream().map(obj -> (JsonObject)obj)
        .map((JsonObject metalRaw) -> {
          return wrapBulkOperation(metalRaw, userId, MetalType.SOURCE);
        }).forEach(operations::add);

    JsonArray mappers = manifest.getJsonArray("mappers");
    mappers.stream().map(obj -> (JsonObject)obj)
        .map((JsonObject metalRaw) -> {
          return wrapBulkOperation(metalRaw, userId, MetalType.MAPPER);
        }).forEach(operations::add);

    JsonArray fusions = manifest.getJsonArray("fusions");
    fusions.stream().map(obj -> (JsonObject)obj)
        .map((JsonObject metalRaw) -> {
          return wrapBulkOperation(metalRaw, userId, MetalType.FUSION);
        }).forEach(operations::add);

    JsonArray sinks = manifest.getJsonArray("sinks");
    sinks.stream().map(obj -> (JsonObject)obj)
        .map((JsonObject metalRaw) -> {
          return wrapBulkOperation(metalRaw, userId, MetalType.SINK);
        }).forEach(operations::add);

    JsonArray setups = manifest.getJsonArray("setups");
    setups.stream().map(obj -> (JsonObject)obj)
        .map((JsonObject metalRaw) -> {
          return wrapBulkOperation(metalRaw, userId, MetalType.SETUP);
        }).forEach(operations::add);

    return mongo.bulkWrite(DB, operations);
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
