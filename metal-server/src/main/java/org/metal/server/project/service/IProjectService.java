package org.metal.server.project.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.Optional;
import org.metal.server.project.Platform;

@ProxyGen
@VertxGen
public interface IProjectService {
  public static IProjectService createProvider(Vertx vertx, MongoClient mongo) {
    IProjectService provider = new ProjectServiceImpl(vertx, mongo);
    return provider;
  }

  public static IProjectService create(Vertx vertx, JsonObject conf) {
    String address = conf.getString("address");
    return new IProjectServiceVertxEBProxy(vertx, address);
  }

  public Future<String> createEmptyProject(
      String userId,
      String projectName);

  public Future<String> createProject(
      String userId,
      String projectName,
      String platform, JsonObject platformArgs, JsonObject backendArgs,
      JsonObject spec
  );

  public Future<String> createProjectFrom(
      String userId, String projectName
  );

  public Future<String> createProjectFromExec(
      String userId, String execId
  );

  public Future<JsonObject> updateName(
      String userId, String projectName, String newProjectName
  );

  public Future<JsonObject> updateSpec(String userId, String projectName, JsonObject spec);

  public Future<JsonObject> updatePlatform(String userId, String projectName, String platform, JsonObject platformArgs, JsonObject backendArgs);

  public Future<JsonObject> updateByPath(String userId, String projectName, JsonObject updateByPath);

  public Future<JsonObject> getOfId(String userId, String projectId);

  public Future<JsonObject> getOfName(String userId, String projectName);

  public Future<List<JsonObject>> getAllOfUser(String userId);

  public Future<List<JsonObject>> getAll();

  public Future<Long> removeOfId(String userId, String projectId);

  public Future<Long> removeOfName(String userId, String projectName);

  public Future<Long> removeAllOfUser(String userId);

  public Future<Long> removeAll();
}
