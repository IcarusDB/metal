package org.metal.server.project.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.uritemplate.UriTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.metal.backend.api.BackendService;
import org.metal.server.api.BackendState;
import org.metal.server.exec.ExecService;
import org.metal.server.util.JsonConvertor;
import org.metal.server.util.SpecJson;

public class ProjectServiceImpl implements IProjectService{
  private final static Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  private MongoClient mongo;
  private Vertx vertx;

  private ExecService execService;
  private WorkerExecutor workerExecutor;
  private JsonObject conf;

  public ProjectServiceImpl(Vertx vertx, MongoClient mongo, WorkerExecutor workerExecutor, ExecService execService, JsonObject conf) {
    this.vertx = vertx;
    this.mongo = mongo;
    this.conf = conf.copy();
    this.workerExecutor = workerExecutor;
    this.execService = execService;
  }

  @Override
  public Future<String> createEmptyProject(String userId, String name) {
    return createProject(userId, name, null, null, null, null);
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
      spec = SpecJson.empty();
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
    return ProjectDBEx.copyFromProject(mongo, userId, name);
  }

  @Override
  public Future<String> createProjectFromWithCopyName(String userId, String name, String copyName) {
    return ProjectDBEx.copyFromProject(mongo, userId, name, copyName);
  }

  @Override
  public Future<String> createProjectFromExec(String userId, String execId) {
    return ProjectDBEx.recoverFromExec(mongo, userId, execId);
  }

  @Override
  public Future<JsonObject> updateProject(
      String userId,
      String id,
      JsonObject detail) {
    String name = null;
    List<String> pkgs = null;
    JsonObject platform = null;
    List<String> backendArgs = null;
    JsonObject spec = null;

    if (detail == null) {
      return Future.failedFuture("The detail is null.");
    }
    try {
      name = detail.getString("name");
      JsonArray pkgArray = detail.getJsonArray("pkgs");
      if (pkgArray != null) {
        pkgs = JsonConvertor.jsonArrayToList(pkgArray);
      }

      platform = detail.getJsonObject("platform");

      JsonArray backendArgArray = detail.getJsonArray("backendArgs");
      if (backendArgArray != null) {
        backendArgs = JsonConvertor.jsonArrayToList(backendArgArray);
      }

      spec = detail.getJsonObject("spec");
    } catch (ClassCastException e) {
      return Future.failedFuture(e);
    }

    return ProjectDBEx.updateProject(mongo, userId, id, name, pkgs, platform, backendArgs, spec);
  }

  @Override
  public Future<JsonObject> updateName(String userId, String name, String newName) {
    return ProjectDBEx.updateName(mongo, userId, name, newName);
  }


  @Override
  public Future<JsonObject> updateSpec(String userId, String projectName, JsonObject spec) {
    return ProjectDBEx.updateSpec(mongo, userId, projectName, spec);
  }

  @Override
  public Future<JsonObject> updatePlatform(String deployId, JsonObject platform) {
    return ProjectDBEx.updatePlatform(mongo, deployId, platform);
  }

  @Override
  public Future<JsonObject> updateBackendArgs(String deployId, List<String> backendArgs) {
    return ProjectDBEx.updateBackendArgs(mongo, deployId, backendArgs);
  }

  @Override
  public Future<JsonObject> updatePkgs(String deployId, List<String> pkgs) {
    return ProjectDBEx.updatePkgs(mongo, deployId, pkgs);
  }

  @Override
  public Future<JsonObject> updateDeployConfsByPath(String deployId, JsonObject updateConfs) {
    return ProjectDBEx.updateDeployConfs(mongo, deployId, updateConfs);
  }

  @Override
  public Future<JsonObject> updateBackendStatus(String deployId, JsonObject updateStatus) {
    return ProjectDBEx.updateBackendStatus(mongo, deployId, updateStatus);
  }

  @Override
  public Future<JsonObject> updateBackendStatusOnUndeploy(String deployId) {
    return ProjectDBEx.updateBackendStatusOnUndeploy(mongo, deployId);
  }

  @Override
  public Future<JsonObject> updateBackendStatusOnUp(String deployId) {
    return ProjectDBEx.updateBackendStatusOnUp(mongo, deployId);
  }

  @Override
  public Future<JsonObject> updateBackendStatusOnDown(String deployId) {
    return ProjectDBEx.updateBackendStatusOnDown(mongo, deployId);
  }

  @Override
  public Future<JsonObject> updateBackendStatusOnFailure(String deployId, String failureMsg) {
    return ProjectDBEx.updateBackendStatusOnFailure(mongo, deployId, failureMsg);
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
  public Future<JsonObject> getDeploymentOfDeployId(String deployId) {
    return ProjectDBEx.getDeployOfDeployId(mongo, deployId);
  }

  @Override
  public Future<JsonObject> getBackendStatusOfDeployId(String deployId) {
    return ProjectDBEx.getBackendStatus(mongo, deployId);
  }

  @Override
  public Future<JsonObject> getSpecOfName(String userId, String name) {
    return ProjectDBEx.getSpecOfName(mongo, userId, name);
  }

  @Override
  public Future<JsonObject> getSpecSchemaOfMetalId(String deployId, String metalId) {
    return ProjectDBEx.getDeployOfDeployId(mongo, deployId).compose((JsonObject deploy) -> {
      if (deploy == null || deploy.isEmpty()) {
        return Future.failedFuture("Fail to get schema, no deploy found.");
      }
      try {
        checkBackendUp(deploy);
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }

      JsonObject address = backendAddress(deploy);
      BackendService backendService = BackendService.create(vertx, address);
      return backendService.schema(metalId);
    });
  }

  @Override
  public Future<JsonObject> heartOfDeployId(String deployId) {
    return ProjectDBEx.getDeployOfDeployId(mongo, deployId).compose((JsonObject deploy) -> {
      if (deploy == null || deploy.isEmpty()) {
        return Future.failedFuture("Fail to get schema, no deploy found.");
      }
      try {
        checkBackendUp(deploy);
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }

      JsonObject address = backendAddress(deploy);
      BackendService backendService = BackendService.create(vertx, address);
      return backendService.heart();
    });
  }

  @Override
  public Future<JsonObject> getBackendServiceStatusOfDeployId(String deployId) {
    return ProjectDBEx.getDeployOfDeployId(mongo, deployId).compose((JsonObject deploy) -> {
      if (deploy == null || deploy.isEmpty()) {
        return Future.failedFuture("Fail to get schema, no deploy found.");
      }
      try {
        checkBackendUp(deploy);
      } catch (IllegalArgumentException e) {
        return Future.failedFuture(e);
      }

      JsonObject address = backendAddress(deploy);
      BackendService backendService = BackendService.create(vertx, address);
      /** TODO
       */
      return backendService.status();
    });
  }

  @Override
  public Future<List<JsonObject>> getAllOfUser(String userId) {
    return ProjectDBEx.getAllOfUser(mongo, userId);
  }

  @Override
  public Future<List<JsonObject>> getAll() {
    return ProjectDBEx.getAll(mongo);
  }

  @Override
  public Future<JsonObject> removeOfId(String userId, String id) {
    return ProjectDBEx.removeOfId(mongo, userId, id);
  }

  @Override
  public Future<JsonObject> removeOfName(String userId, String name) {
    return ProjectDBEx.removeOfName(mongo, userId, name);
  }

  @Override
  public Future<JsonObject> removeAllOfUser(String userId) {
    return ProjectDBEx.removeAllOfUser(mongo, userId);
  }

  @Override
  public Future<JsonObject> removeAll() {
    return ProjectDBEx.removeAll(mongo);
  }

  @Override
  public Future<JsonObject> deploy(String userId, String name) {
    return ProjectDBEx.increaseDeployEpoch(mongo, userId, name).compose(ret -> {
      return getOfName(userId, name);
    }).compose((JsonObject project) -> {
      JsonObject deploy = project.getJsonObject(ProjectDBEx.DEPLOY);
      return onDeploy(deploy);
    });
  }

  @Override
  public Future<JsonObject> deployOfId(String deployId) {
    return ProjectDBEx.increaseDeployEpoch(mongo, deployId).compose(ret -> {
      return getDeploymentOfDeployId(deployId);
    }).compose((JsonObject deploy) -> {
      return onDeploy(deploy);
    });
  }

  private Future<JsonObject> onDeploy(JsonObject deploy) {
    JsonObject backend = deploy.getJsonObject(ProjectDBEx.DEPLOY_BACKEND);
    JsonObject backendStatus = backend.getJsonObject(ProjectDBEx.DEPLOY_BACKEND_STATUS);
    if (backendStatus != null && !backendStatus.isEmpty()) {
      return Future.failedFuture(
          "One backend has been deployed. User can\'t deploy the other new backend before that the deployed backend is cleaned.");
    }

    String deployId = deploy.getString(ProjectDBEx.DEPLOY_ID);
    int epoch = deploy.getInteger(ProjectDBEx.DEPLOY_EPOCH);
    List<String> pkgs = JsonConvertor.jsonArrayToList(deploy.getJsonArray(ProjectDBEx.DEPLOY_PKGS));
    JsonObject platform = deploy.getJsonObject(ProjectDBEx.DEPLOY_PLATFORM);
    List<String> backendArgs = JsonConvertor.jsonArrayToList(backend.getJsonArray(ProjectDBEx.DEPLOY_BACKEND_ARGS));

    backendArgs = antiInject(backendArgs);
    String reportServiceAddress = conf.getJsonObject("backendReportService").getString("address");
    List<String> defaultBackendArgs = List.of(
        "--interactive-mode",
        "--deploy-id", deployId,
        "--deploy-epoch", String.valueOf(epoch),
        "--report-service-address", reportServiceAddress,
        "--rest-api-port", String.valueOf(18000)
    );

    List<String> appArgs = new ArrayList<>();
    appArgs.addAll(defaultBackendArgs);
    appArgs.addAll(backendArgs);

    if (platform.fieldNames().contains("spark.standalone")) {
      try {
        JsonObject sparkStandalone = platform.getJsonObject("spark.standalone");
        if (sparkStandalone == null || sparkStandalone.isEmpty()) {
          return Future.failedFuture(
              String.format("Fail deploy [%s-%d]. No spark.standalone configurations found.",
                  deployId, epoch));
        }
        return sparkStandaloneDeploy(deployId, epoch, appArgs, sparkStandalone);
      } catch (Exception e) {
        return Future.failedFuture(e);
      }
    }

    return Future.failedFuture("Fail to found any legal platform configuration.");
  }

  @Override
  public Future<JsonObject> reDeploy(String userId, String name) {
    return getOfName(userId, name).compose((JsonObject project) -> {
      try {
        JsonObject deploy = project.getJsonObject(ProjectDBEx.DEPLOY);
        return reDeploy(deploy);
      } catch (Exception e) {
        return Future.failedFuture(e);
      }
    });
  }

  @Override
  public Future<JsonObject> reDeployOfId(String userId, String deployId) {
    return getDeploymentOfDeployId(deployId).compose((JsonObject deploy) -> {
      return reDeploy(deploy);
    });
  }

  private Future<JsonObject> reDeploy(JsonObject deploy ) {
    try {
      if (deploy == null || deploy.isEmpty()) {
        String msg = "Fail to reDeploy, no deploy found.";
        LOGGER.error(msg);
        return Future.failedFuture(msg);
      }

      String deployId = deploy.getString(ProjectDBEx.DEPLOY_ID);
      if (deployId == null || deployId.isBlank()) {
        String msg = "Fail to reDeploy, no deploy id found.";
        LOGGER.error(msg);
        return Future.failedFuture(msg);
      }

      return forceKillBackend(deployId).compose((JsonObject ret) -> {
        return ProjectDBEx.increaseDeployEpoch(mongo, deployId);
      }).compose((JsonObject ret) -> {
        return deployOfId(deployId);
      });
    } catch (Exception e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    }
  }

  private Future<JsonObject> sparkStandaloneDeploy(String deployId, int epoch, List<String> appArgs,
      JsonObject sparkStandalone) {
    JsonObject restApi = sparkStandalone.getJsonObject("rest.api");
    JsonObject conf = sparkStandalone.getJsonObject("conf");
    conf.put("appArgs", appArgs);

    WebClientOptions options = new WebClientOptions();
    String restApiHost = restApi.getString("host");
    int restApiPort = restApi.getInteger("port");
    WebClient webClient = WebClient.create(vertx);
    UriTemplate createURI = UriTemplate.of(restApi.getJsonObject("requestURI").getString("create"));

    return ProjectDBEx.updateBackendStatusOnUndeploy(mongo, deployId).compose(ret -> {
      return webClient.post(restApiPort, restApiHost, createURI)
          .sendJsonObject(conf);
    }).compose((HttpResponse<Buffer> response) -> {
      try {
        JsonObject resp = response.bodyAsJsonObject();
        Boolean isSuccess = resp.getBoolean("success");
        if (isSuccess == null || isSuccess == false) {
          return Future.failedFuture(
              String.format("Fail to deploy[%s-%d]. %s.", deployId, epoch, resp.toString()));
        }
        String driverId = resp.getString("submissionId");
        JsonObject tracer = new JsonObject()
            .put("driverId", driverId);
        return ProjectDBEx.updateBackendStatusTracer(mongo, deployId, tracer)
            .compose(ret -> {
              return Future.succeededFuture(resp);
            });
      } catch (Exception e) {
        return Future.failedFuture(e);
      }
    });
  }

  @Override
  public Future<JsonObject> forceKillBackend(String deployId) {
    return ProjectDBEx.getDeployOfDeployId(mongo, deployId).compose((JsonObject deploy) -> {
      JsonObject platform = deploy.getJsonObject(ProjectDBEx.DEPLOY_PLATFORM);
      JsonObject backend = deploy.getJsonObject(ProjectDBEx.DEPLOY_BACKEND);
      if (backend == null || backend.isEmpty()) {
        return Future.failedFuture("Fail to froce kill, no backend configuration found.");
      }

      JsonObject backendStatus = backend.getJsonObject(ProjectDBEx.DEPLOY_BACKEND_STATUS);
      if (backendStatus == null || backendStatus.isEmpty()) {
        return Future.failedFuture("Fail to force kill, no status found.");
      }

      JsonObject tracer = backendStatus.getJsonObject(ProjectDBEx.DEPLOY_BACKEND_STATUS_TRACER);
      if (tracer == null || tracer.isEmpty()) {
        return Future.failedFuture("Fail to force kill, no tracer found.");
      }

      if (platform == null || platform.isEmpty()) {
        return Future.failedFuture("Fail to force kill, no platform configuration found.");
      }

      if (platform.containsKey("spark.standalone")) {
        try {
          JsonObject restApi = platform.getJsonObject("spark.standalone").getJsonObject("rest.api");
          if (restApi == null || restApi.isEmpty()) {
            return Future.failedFuture("Fail to force kill, no rest api found.");
          }
          return sparkStandaloneForceKill(tracer, restApi).compose((JsonObject resp) -> {
            return ProjectDBEx.removeBackendStatusOfDeployId(mongo, deployId)
                .compose(ret -> {
                  return Future.succeededFuture(resp);
                });
          }, error -> {
            LOGGER.error(error);
            return heartOfDeployId(deployId).compose(ret -> {
              return sparkStandaloneForceKill(tracer, restApi);
            }).compose(ret -> {
              return ProjectDBEx.removeBackendStatusOfDeployId(mongo, deployId).compose(v -> {
                return Future.succeededFuture(ret);
              });
            }, err -> {
              return ProjectDBEx.removeBackendStatusOfDeployId(mongo, deployId)
                  .compose(ret -> {
                    JsonObject resp = new JsonObject();
                    resp.put("Status", "OK")
                        .put("msg", err.getLocalizedMessage());
                    return Future.succeededFuture(resp);
                  });
            });
          });
        } catch (ClassCastException e) {
          return Future.failedFuture(e);
        }
      }

      return Future.failedFuture("Fail to force kill, no support platform.");
    });
  }

  private boolean checkBackendUp(JsonObject deploy) throws IllegalArgumentException {
    try {
      JsonObject backend = deploy.getJsonObject(ProjectDBEx.DEPLOY_BACKEND);
      JsonObject backendStatus = backend.getJsonObject(ProjectDBEx.DEPLOY_BACKEND_STATUS);
      if (backendStatus == null || backendStatus.isEmpty()) {
        throw new IllegalArgumentException("The backend is not UP.");
      }
      BackendState current = BackendState.valueOf(backendStatus.getString(ProjectDBEx.DEPLOY_BACKEND_STATUS_CURRENT));
      if (!current.equals(BackendState.UP)) {
        throw new IllegalArgumentException("The backend is not UP.");
      }
      return true;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private JsonObject backendAddress(JsonObject deploy) {
    try {
      JsonObject backend = deploy.getJsonObject(ProjectDBEx.DEPLOY_BACKEND);
      Integer epoch = deploy.getInteger(ProjectDBEx.DEPLOY_EPOCH);
      String deployId = deploy.getString(ProjectDBEx.DEPLOY_ID);
      if (epoch == null) {
        throw new IllegalArgumentException("Fail to get address, the epoch of backend is not existed.");
      }
      if (deployId == null || deployId.isBlank()) {
        throw new IllegalArgumentException("Fail to get address, the deploy id of backend is not existed.");
      }
      return new JsonObject().put("address", deployId + "-" + epoch);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Future<JsonObject> analysis(String userId, String name, JsonObject spec) {
    return ProjectDBEx.getOfName(mongo, userId, name).compose((JsonObject proj) -> {
      return analysisSpec(userId, spec, proj);
    });
  }

  private Future<JsonObject> analysisSpec(String userId, JsonObject spec, JsonObject proj) {
    try {
      String projectName = proj.getString(ProjectDBEx.NAME);
      JsonObject deploy = proj.getJsonObject(ProjectDBEx.DEPLOY);
      checkBackendUp(deploy);
      JsonObject address = backendAddress(deploy);
      return ProjectDBEx.updateSpec(mongo, userId, projectName, spec).compose((JsonObject ret) -> {
        BackendService backendService = BackendService.create(vertx, address);
        return backendService.analyse(spec);
      });
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> analysisOfId(String userId, String id, JsonObject spec) {
    return ProjectDBEx.getOfId(mongo, userId, id).compose((JsonObject proj) -> {
      return analysisSpec(userId, spec, proj);
    });
  }

  @Override
  public Future<JsonObject> exec(String userId, String name) {
    return ProjectDBEx.getOfName(mongo, userId, name).compose((JsonObject proj) -> {
      return execProject(userId, proj);
    });
  }

  @Override
  public Future<JsonObject> execOfId(String userId, String id) {
    return ProjectDBEx.getOfId(mongo, userId, id).compose((JsonObject proj) -> {
      return execProject(userId, proj);
    });
  }

  private Future<JsonObject> execProject(String userId, JsonObject proj) {
    try {
      JsonObject deploy = proj.getJsonObject(ProjectDBEx.DEPLOY);
      checkBackendUp(deploy);
      JsonObject address = backendAddress(deploy);
      return execService.add(userId, proj).compose((String execId) -> {
        BackendService backendService = BackendService.create(vertx, address);
        JsonObject execArgs = new JsonObject();
        execArgs.put("id", execId);
        return backendService.exec(execArgs).compose(r -> {
          return Future.succeededFuture(new JsonObject().put("status", "OK"));
        });
      });
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  private Future<JsonObject> sparkStandaloneForceKill(JsonObject tracer, JsonObject restApi) {
    if (tracer == null || !tracer.containsKey("driverId")) {
      return Future.failedFuture("Fail to force kill spark in standalone, none driverId found in tracer.");
    }
    try {
      String driverId = tracer.getString("driverId");
      String host = restApi.getString("host");
      int port = restApi.getInteger("port");
      String killTemplate = restApi.getJsonObject("requestURI").getString("kill");
      WebClient webClient = WebClient.create(vertx);
      UriTemplate killUri = UriTemplate.of(killTemplate);
      return webClient.post(port, host, killUri)
          .setTemplateParam("driverId", driverId)
          .send().compose((HttpResponse<Buffer> response) -> {
            return Future.succeededFuture(response.bodyAsJsonObject());
          }).compose((JsonObject resp) -> {
            try {
              Boolean isSuccess = resp.getBoolean("success");
              if (isSuccess == null || isSuccess == false) {
                return Future.failedFuture("Fail to kill spark standalone backend.");
              }
              return Future.succeededFuture(resp);
            } catch (ClassCastException e) {
              return Future.failedFuture(e);
            }
          });
    } catch (ClassCastException | NullPointerException e) {
      return Future.failedFuture(e);
    }
  }

  private static List<String> antiInject(List<String> backendArgs) {
    List<String> ret = new ArrayList<>();
    for(int idx = 0; idx < backendArgs.size(); idx++) {
      String arg = backendArgs.get(idx).strip();
      if (arg.equals("--interactive-mode") || arg.equals("--cmd-mode")) {
        continue;
      }

      if (arg.equals("--deploy-id")) {
        idx++;
        continue;
      }

      if (arg.equals("--deploy-epoch")) {
        idx++;
        continue;
      }

      if (arg.equals("--report-service-address")) {
        idx++;
        continue;
      }

      ret.add(arg);
    }

   return Collections.unmodifiableList(ret);
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
