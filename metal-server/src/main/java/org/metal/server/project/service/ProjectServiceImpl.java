package org.metal.server.project.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.metal.server.api.BackendState;
import org.metal.server.project.Platform;
import org.metal.server.project.ProjectDB;
import org.metal.server.util.ReadStreamCollector;

public class ProjectServiceImpl implements IProjectService{
  private final static Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  private MongoClient mongo;
  private Vertx vertx;
  private WorkerExecutor workerExecutor;
  private JsonObject conf;

  public ProjectServiceImpl(Vertx vertx, MongoClient mongo, WorkerExecutor workerExecutor, JsonObject conf) {
    this.vertx = vertx;
    this.mongo = mongo;
    this.conf = conf.copy();
    this.workerExecutor = workerExecutor;
  }

  @Override
  public Future<String> createEmptyProject(String userId, String name) {
    return createProject(userId, name, null, null, null, null);
  }

  private static JsonObject emptySpec() {
    return new JsonObject()
        .put("version", "1.0")
        .put("metals", new JsonArray())
        .put("edges", new JsonArray());
  }

  @Override
  public Future<String> createProject(
      String userId,
      String name,
      List<String> pkgs,
      JsonObject platform,
      List<String> backendArgs,
      JsonObject spec) {
    if (pkgs == null) {
      pkgs = new ArrayList<>();
    }
    if (backendArgs == null) {
      backendArgs = new ArrayList<>();
    }
    if (spec == null || spec.isEmpty()) {
      spec = emptySpec();
    }
    if (platform == null || platform.isEmpty()) {
      LOGGER.info("Platform is not set and will be replaced with the default platform.");
      JsonObject defaultPlatform = conf.getJsonObject("platform");
      if (defaultPlatform == null || defaultPlatform.isEmpty()) {
        LOGGER.error("Fail to load default platform.");
        return Future.failedFuture("Fail to load default platform.");
      } else {
        platform = defaultPlatform;
      }
    }

    return ProjectDBEx.add(
        mongo,
        userId,
        name,
        pkgs,
        platform,
        backendArgs,
        spec
    );
  }

  @Override
  public Future<String> createProjectFrom(String userId, String name) {
    return ProjectDB.copyFrom(mongo, userId, name);
  }

  @Override
  public Future<String> createProjectFromExec(String userId, String execId) {
    return ProjectDB.recoverFrom(mongo, userId, execId);
  }

  @Override
  public Future<JsonObject> updateName(String userId, String name, String newName) {
    return ProjectDB.updateProjectName(mongo, userId, name, newName);
  }



  @Override
  public Future<JsonObject> updateSpec(String userId, String projectName, JsonObject spec) {
    return ProjectDB.update(
        mongo,
        userId,
        projectName,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(spec)
    );
  }

  @Override
  public Future<JsonObject> updatePlatform(String userId, String projectName, String platform,
      JsonObject platformArgs, JsonObject backendArgs) {
    return ProjectDB.update(
        mongo,
        userId,
        projectName,
        Optional.of(Platform.valueOf(platform)),
        Optional.of(platformArgs),
        Optional.of(backendArgs),
        Optional.empty()
    );
  }

  @Override
  public Future<JsonObject> updateByPath(String userId, String projectName, JsonObject updateByPath) {
    return ProjectDB.updateByPath(mongo, userId, projectName, updateByPath);
  }

  @Override
  public Future<JsonObject> updateStatus(String deployId, JsonObject updateStatus) {
    return ProjectDB.updateBackendStatus(mongo, deployId, updateStatus);
  }

  @Override
  public Future<JsonObject> getOfId(String userId, String projectId) {
    return ProjectDBEx.getOfId(mongo, userId, projectId);
  }

  @Override
  public Future<JsonObject> getOfName(String userId, String projectName) {
    return ProjectDBEx.getOfName(mongo, userId, projectName);
  }

  @Override
  public Future<JsonObject> getBackendStatusOfDeployId(String deployId) {
    return ProjectDB.getBackendStatusOfDeployId(mongo, deployId);
  }

  @Override
  public Future<List<JsonObject>> getAllOfUser(String userId) {
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.getAllOfUser(mongo, userId)
    );
  }

  @Override
  public Future<List<JsonObject>> getAll() {
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.getAll(mongo)
    );
  }

  @Override
  public Future<JsonObject> removeOfId(String userId, String projectId) {
    return ProjectDB.removeOfId(mongo, userId, projectId);
  }

  @Override
  public Future<JsonObject> removeOfName(String userId, String name) {
    return ProjectDB.removeOfName(mongo, userId, name);
  }

  @Override
  public Future<JsonObject> removeAllOfUser(String userId) {
    return ProjectDB.removeAllOfUser(mongo, userId);
  }

  @Override
  public Future<JsonObject> removeAll() {
    return ProjectDB.removeAll(mongo);
  }

  @Override
  public Future<JsonObject> deploy(String userId, String name) {
    return getOfName(userId, name).compose((JsonObject project) -> {
      String deployId = project.getString(ProjectDB.FIELD_DEPLOY_ID);
      if (deployId == null || deployId.strip().isEmpty()) {
        return Future.failedFuture("deployId is not set.");
      }

      JsonObject deployArgs = project.getJsonObject(ProjectDB.FIELD_DEPLOY_ARGS);
      if (deployArgs == null || deployArgs.isEmpty()) {
        return Future.failedFuture("deployArgs is not set.");
      }

      String platform = deployArgs.getString(ProjectDB.FIELD_DEPLOY_ARGS_PLATFORM);
      if (platform == null) {
        return Future.failedFuture("platform is not set.");
      }
      try {
        Platform.valueOf(platform);
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }

      JsonObject platformArgs = deployArgs.getJsonObject(ProjectDB.FIELD_DEPLOY_ARGS_PLATFORM_ARGS);
      JsonArray platformArgsArgs = platformArgs.getJsonArray(ProjectDB.FIELD_DEPLOY_ARGS_PLATFORM_ARGS_ARGS);
      JsonArray plataformArgsPkgs = platformArgs.getJsonArray(ProjectDB.FIELD_DEPLOY_ARGS_PLATFORM_ARGS_PKGS, new JsonArray());
      if (platformArgs == null || platformArgs.isEmpty()) {
        return Future.failedFuture("platformArgs is not set.");
      }
      if (platformArgsArgs == null) {
        return Future.failedFuture("platformArgs.args is not set.");
      }

      List<String> parseArgs = new ArrayList<>();
      switch (Platform.valueOf(platform)) {
        case SPARK: {
          try {
            parseArgs.addAll(parsePlatformArgs(platformArgsArgs, plataformArgsPkgs));
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }
        }; break;
        default: {
          return Future.failedFuture(String.format("%s is not supported.", platform));
        }
      }

      String backendJar = conf.getString("backendJar");
      if (backendJar == null || backendJar.strip().isEmpty()) {
        LOGGER.error("backendJar is not configured.");
        return Future.failedFuture("backendJar is not configured.");
      }
      parseArgs.add(backendJar);

      JsonObject backendArgs = deployArgs.getJsonObject(ProjectDB.FIELD_DEPLOY_ARGS_BACKEND_ARGS);
      if (backendArgs == null || backendArgs.isEmpty()) {
        return Future.failedFuture("backendArgs is not set.");
      }

      JsonArray backendArgsArgs = backendArgs.getJsonArray(ProjectDB.FIELD_DEPLOY_ARGS_BACKEND_ARGS_ARGS);
      try {
        parseArgs.addAll(parseBackendArgs(backendArgsArgs));
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }

      JsonObject backendStatus = project.getJsonObject(ProjectDB.FIELD_BACKEND_STATUS);
      if (backendStatus == null || backendStatus.isEmpty()) {
        return Future.failedFuture("backendStatus is not set.");
      }

      String backendStatusStatus = backendStatus.getString(ProjectDB.FIELD_BACKEND_STATUS_STATUS);
      if (backendStatusStatus == null || backendStatusStatus.isBlank()) {
        return Future.failedFuture("backendStatus.status is not set.");
      }
      try {
        BackendState.valueOf(backendStatusStatus);
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }
      if (!BackendState.valueOf(backendStatusStatus).equals(BackendState.UN_DEPLOY)) {
        return Future.failedFuture(String.format("Fail to deploy backend, because backend is in %s.", backendStatusStatus));
      }

      int epoch = backendStatus.getInteger(ProjectDB.FIELD_BACKEND_STATUS_EPOCH, ProjectDB.DEFAULT_EPOCH);
      if (epoch != -1) {
        String errorMsg = "The epoch of undeploy backend should be -1, now is " + epoch;
        LOGGER.error(errorMsg);
        return Future.failedFuture(errorMsg);
      }
      parseArgs.add("--interactive-mode ");
      parseArgs.add("--deploy-id");
      parseArgs.add(deployId);
      parseArgs.add("--deploy-epoch");
      parseArgs.add(String.valueOf(epoch));

      String reportServiceAddress = conf.getJsonObject("backendReportService").getString("address");
      parseArgs.add("--report-service-address");
      parseArgs.add(reportServiceAddress);
      parseArgs.add("--rest-api-port");
      parseArgs.add("18989");

      System.out.println(parseArgs);
      switch (Platform.valueOf(platform)) {
        case SPARK: {
//          String deployer = "org.metal.backend.spark.SparkBackendDeploy";
//          Optional<IBackendDeploy> backendDeploy = BackendDeployManager.getBackendDeploy(deployer);
//          if (backendDeploy.isEmpty()) {
//            return Future.failedFuture(String.format("Fail to create IBackendDeploy[%s] instance.", deployer));
//          }
//          return workerExecutor.executeBlocking((promise)->{
//            try {
//              backendDeploy.get().deploy(parseArgs.<String>toArray(String[]::new));
//              promise.complete();
//            } catch (Exception e) {
//              promise.fail(e);
//            }
//          }, true);
        }
        default: {
          return Future.failedFuture(String.format("%s is not supported.", platform));
        }
      }
    });
  }

  private List<String> parsePlatformArgs(JsonArray platformArgs, JsonArray platformPkgs) throws IllegalArgumentException{
    List<String> args = platformArgs.stream().map(Object::toString).collect(Collectors.toList());
    boolean classArgReady = false;
    try {
      for(int idx = 0; idx < args.size(); idx++) {
        String arg = args.get(idx);
        if ("--class".equals(arg)) {
          String classArg = args.get(idx + 1);
//          if (BackendLauncher.class.toString().equals(classArg)) {
//            classArgReady = true;
//          } else {
//            throw new IllegalArgumentException("platformArgs.args --class is set wrong.");
//          }
        }
      }
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(e);
    }

//    if (!classArgReady) {
//      args.add("--class");
//      args.add(BackendLauncher.class.toString());
//    }

    String packagesArg = platformPkgs.stream().map(Object::toString).collect(Collectors.joining(","));
    if (!platformPkgs.isEmpty()) {
      args.add("--packages");
      args.add(packagesArg);
    }
    return args;
  }

  private List<String> parseBackendArgs(JsonArray backendArgs) throws IllegalArgumentException{
    List<String> args = backendArgs.stream().map(Object::toString).collect(Collectors.toList());

    boolean interactiveReady = false;
    for(int idx = 0; idx < args.size(); idx++) {
      String arg = args.get(idx);
      if ("--interactive-mode".equals(arg)) {
        interactiveReady = true;
      }
    }

    if (!interactiveReady){
      args.add("--interactive-mode");
    }

    return args;
  }
}
