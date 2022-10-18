package org.metal.server.repo.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.Optional;
import org.metal.server.repo.MetalRepoDB;
import org.metal.server.repo.MetalRepoDB.MetalScope;
import org.metal.server.repo.MetalRepoDB.MetalType;
import org.metal.server.util.ReadStreamCollector;


public class MetalRepoServiceImpl implements IMetalRepoService{
  private MongoClient mongo;
  private JsonObject conf;

  public MetalRepoServiceImpl(MongoClient mongo, JsonObject conf) {
    this.mongo = mongo;
    this.conf = conf;
  }

  private boolean checkMetalScope(String scope) throws IllegalArgumentException {
    MetalScope.valueOf(scope);
    return true;
  }

  private boolean checkMetalType(String type) throws IllegalArgumentException {
    MetalType.valueOf(type);
    return true;
  }

  private boolean checkMetal(JsonObject metal) throws IllegalArgumentException{
    String pkg = metal.getString("pkg") ;
    if (pkg == null || pkg.isBlank()) {
      throw new IllegalArgumentException(
        String.format("Fail to found pkg value in %s.", metal.toString())
      );
    }

    String clazz = metal.getString("class");
    if (clazz == null || clazz.isBlank()) {
      throw new IllegalArgumentException(
          String.format("Fail to found class value in %s.", metal.toString())
      );
    }

    try {
      JsonObject formSchema = metal.getJsonObject("formSchema");
      if (formSchema == null || formSchema.isEmpty()) {
        throw new IllegalArgumentException(
            String.format("Fail to found formSchema value in %s.", metal.toString())
        );
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    try {
      metal.getJsonObject("uiSchema");
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return true;
  }

  private boolean checkManifest(JsonObject manifest) throws IllegalArgumentException {
    String[] metalTypes = new String[]{
        "sources", "mappers", "fusions", "sinks", "setups"
    };

    for (String metalType: metalTypes) {
      JsonArray metals = manifest.getJsonArray(metalType, new JsonArray());
      for(int idx = 0; idx < metals.size(); idx++) {
        JsonObject metal = metals.getJsonObject(idx);
        checkMetal(metal);
      }
    }
    return true;
  }

  public Future<String> add(String userId, String type, String scope, JsonObject metal) {
    try {
      checkMetalType(type);
      checkMetalScope(scope);
      checkMetal(metal);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    MetalType metalType = MetalType.valueOf(type);
    MetalScope metalScope = MetalScope.valueOf(scope);
    String pkg = metal.getString("pkg");
    String clazz = metal.getString("class");
    JsonObject formSchema = metal.getJsonObject("formSchema");
    Optional<JsonObject> uiSchema = Optional.ofNullable(metal.getJsonObject("uiSchema"));

    return MetalRepoDB.add(
        mongo,
        userId,
        metalType,
        metalScope,
        pkg,
        clazz,
        formSchema,
        uiSchema
    );
  };

  public Future<JsonObject> get(String userId, String metalId) {
    return MetalRepoDB.get(mongo, userId, metalId);
  }

  public Future<List<JsonObject>> getAllOfUser(String userId) {
    return ReadStreamCollector.<JsonObject>toList(
        MetalRepoDB.getAllOfUser(mongo, userId)
    );
  }

  public Future<List<JsonObject>> getAllOfUserScope(String userId, String scope) {
    try {
      checkMetalScope(scope);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    MetalScope metalScope = MetalScope.valueOf(scope);
    return ReadStreamCollector.<JsonObject>toList(
      MetalRepoDB.getAllOfUserScope(mongo, userId, metalScope)
    );
  }

  public Future<List<JsonObject>> getAllOfPublic() {
    return ReadStreamCollector.<JsonObject>toList(
        MetalRepoDB.getAllOfPublic(mongo)
    );
  }

  public Future<JsonObject> addFromManifest(String userId, String scope, JsonObject manifest) {
    try {
      checkMetalScope(scope);
      checkManifest(manifest);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    MetalScope metalScope = MetalScope.valueOf(scope);
    return MetalRepoDB.addFromManifest(mongo, userId, metalScope, manifest)
        .compose(result -> {
          return Future.succeededFuture(result.toJson());
        });
  }

  public Future<JsonObject> removePrivate(String userId, String metalId) {
    return MetalRepoDB.removePrivate(mongo, userId, metalId)
        .compose(result -> {
          return Future.succeededFuture(result.toJson());
        });
  }

  public Future<JsonObject> removeAllPrivateOfUser(String userId) {
    return MetalRepoDB.removeAllPrivateOfUser(mongo, userId)
        .compose(result -> {
          return Future.succeededFuture(result.toJson());
        });
  }

  public Future<JsonObject> removeAll() {
    return MetalRepoDB.removeAll(mongo)
        .compose(result -> {
          return Future.succeededFuture(result.toJson());
        });
  }
}
