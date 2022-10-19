package org.metal.server.project.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
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
  public static IProjectService createProvider(Vertx vertx, MongoClient mongo, WorkerExecutor workerExecutor, JsonObject conf) {
    IProjectService provider = new ProjectServiceImpl(vertx, mongo, workerExecutor, conf);
    return provider;
  }

  public static IProjectService create(Vertx vertx, JsonObject conf) {
    String address = conf.getString("address");
    return new IProjectServiceVertxEBProxy(vertx, address);
  }

  public Future<String> createEmptyProject(
      String userId,
      String name);

  public Future<String> createProject(
      String userId,
      String name,
      List<String> pkgs,
      JsonObject platform,
      List<String> backendArgs,
      JsonObject spec
  );


  public Future<String> createProjectFrom(
      String userId, String name
  );

  public Future<String> createProjectFromExec(
      String userId, String execId
  );

  public Future<JsonObject> updateName(
      String userId, String name, String newName
  );

  public Future<JsonObject> updateSpec(String userId, String name, JsonObject spec);

  public Future<JsonObject> updatePlatform(String userId, String name, String platform, JsonObject platformArgs, JsonObject backendArgs);

  public Future<JsonObject> updateByPath(String userId, String name, JsonObject updateByPath);

  public Future<JsonObject> updateStatus(String deployId, JsonObject updateStatus);

  public Future<JsonObject> getOfId(String userId, String projectId);

  public Future<JsonObject> getOfName(String userId, String name);

  public Future<JsonObject> getBackendStatusOfDeployId(String deployId);

  public Future<List<JsonObject>> getAllOfUser(String userId);

  public Future<List<JsonObject>> getAll();

  public Future<JsonObject> removeOfId(String userId, String projectId);

  public Future<JsonObject> removeOfName(String userId, String name);

  public Future<JsonObject> removeAllOfUser(String userId);

  public Future<JsonObject> removeAll();

  public Future<JsonObject> deploy(String userId, String name);
}
