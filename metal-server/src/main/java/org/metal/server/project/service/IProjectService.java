package org.metal.server.project.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import org.metal.server.api.BackendState;
import org.metal.server.exec.ExecService;

@ProxyGen
@VertxGen
public interface IProjectService {
  public static IProjectService createProvider(Vertx vertx, MongoClient mongo, WorkerExecutor workerExecutor, ExecService execService, JsonObject conf) {
    IProjectService provider = new ProjectServiceImpl(vertx, mongo, workerExecutor, execService, conf);
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

  public Future<String> createProjectFromWithCopyName(
      String userId, String name, String copyName
  );

  public Future<String> createProjectFromExec(
      String userId, String execId
  );

  public Future<JsonObject> updateProject(
      String userId,
      String id,
      JsonObject detail
  );

  public Future<JsonObject> updateName(
      String userId, String name, String newName
  );

  public Future<JsonObject> updateSpec(String userId, String name, JsonObject spec);

  public Future<JsonObject> updatePlatform(String deployId, JsonObject platform);

  public Future<JsonObject> updateBackendArgs(String deployId, List<String> backendArgs);

  public Future<JsonObject> updatePkgs(String deployId, List<String> pkgs);

  public Future<JsonObject> updateDeployConfsByPath(String deployId, JsonObject updateConfs);

  public Future<JsonObject> updateBackendStatus(String deployId, JsonObject updateStatus);

  public Future<JsonObject> updateBackendStatusOnUndeploy(String deployId);

  public Future<JsonObject> updateBackendStatusOnCreated(String deployId);

  public Future<JsonObject> updateBackendStatusOnUp(String deployId);

  public Future<JsonObject> updateBackendStatusOnDown(String deployId);

  public Future<JsonObject> updateBackendStatusOnFailure(String deployId, String failureMsg);

  public Future<JsonObject> updateBackendStatusOnCreatedWith(String deployId, int epoch, BackendState current);

  public Future<JsonObject> updateBackendStatusOnUpWith(String deployId, int epoch, BackendState current);

  public Future<JsonObject> updateBackendStatusOnDownWith(String deployId, int epoch, BackendState current);

  public Future<JsonObject> updateBackendStatusOnFailureWith(String deployId, int epoch, BackendState current, String failureMsg);

  public Future<JsonObject> getOfId(String userId, String projectId);

  public Future<JsonObject> getOfName(String userId, String name);

  public Future<JsonObject> getSpecOfName(String userId, String name);

  public Future<JsonObject> getSpecSchemaOfMetalId(String deployId, String metalId);

  public Future<JsonObject> getDeploymentOfDeployId(String deployId);
  public Future<JsonObject> getBackendStatusOfDeployId(String deployId);

  public Future<JsonObject> getDeploymentOfDeployIdWithEpoch(String deployId, int epoch);
  public Future<JsonObject> getBackendStatusOfDeployIdWithEpoch(String deployId, int epoch);

  public Future<List<JsonObject>> getAllOfUser(String userId);

  public Future<List<JsonObject>> getAll();

  public Future<JsonObject> heartOfDeployId(String deployId);

  public Future<JsonObject> getBackendServiceStatusOfDeployId(String deployId);

  public Future<JsonObject> removeOfId(String userId, String projectId);

  public Future<JsonObject> removeOfName(String userId, String name);

  public Future<JsonObject> removeAllOfUser(String userId);

  public Future<JsonObject> removeAll();

  public Future<JsonObject> deploy(String userId, String name);

  public Future<JsonObject> deployOfId(String deployId);

  public Future<JsonObject> forceKillBackend(String deployId);

  public Future<JsonObject> reDeploy(String userId, String name);

  public Future<JsonObject> reDeployOfId(String userId, String deployId);

  public Future<JsonObject> analysis(String userId, String name, JsonObject spec);

  public Future<JsonObject> analysisOfId(String userId, String id, JsonObject spec);

  public Future<JsonObject> saveSpecOfId(String userId, String id, JsonObject spec);

  public Future<JsonObject> analysisSubSpecOfId(String userId, String id, JsonObject spec, JsonObject subSpec);

  public Future<JsonObject> exec(String userId, String name);

  public Future<JsonObject> execOfId(String userId, String id);
}
