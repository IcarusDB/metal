package org.metal.server.repo.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;

@ProxyGen
@VertxGen
public interface IMetalRepoService {
  public static IMetalRepoService create(Vertx vertx, JsonObject conf) {
    String address = conf.getString("address");
    return new IMetalRepoServiceVertxEBProxy(vertx, address);
  }

  public static IMetalRepoService createProvider(Vertx vertx, MongoClient mongo, JsonObject conf) {
    return new MetalRepoServiceImpl(mongo, conf);
  }

  public Future<String> add(String userId, String type, String scope, JsonObject metal);

  public Future<JsonObject> get(String userId, String metalId);

  public Future<List<JsonObject>> getAllOfUser(String userId);

  public Future<List<JsonObject>> getAllOfUserScope(String userId, String scope);

  public Future<List<JsonObject>> getAllOfPublic();

  public Future<JsonObject> addFromManifest(String userId, String scope, JsonObject manifest);

  public Future<JsonObject> removePrivate(String userId, String metalId);

  public Future<JsonObject> removeAllPrivateOfUser(String userId);

  public Future<JsonObject> removeAll();
}
