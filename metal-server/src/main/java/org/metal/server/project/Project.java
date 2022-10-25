package org.metal.server.project;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.List;
import org.metal.server.util.JsonConvertor;
import org.metal.server.util.RestServiceEnd;
import org.metal.server.util.OnFailure;
import org.metal.server.util.SendJson;
import org.metal.server.project.service.IProjectService;
import org.metal.server.util.SpecJson;

public class Project extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(Project.class);
  public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
  public static final String MONGO_CONF = "mongoConf";
  public static final String PROJECT_CONF = "project";
  public static final String PROJECT_SERVICE_CONF = "projectService";
  public static final String PROJECT_SERVICE_ADDRESS_CONF = "address";

  private MongoClient mongo;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private IProjectService provider;
  private WorkerExecutor workerExecutor;

  private Project() {}

  public static Project create() {
    return new Project();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ConfigStoreOptions fileConfigStoreOptions = new ConfigStoreOptions()
        .setType("file")
        .setConfig(new JsonObject().put("path", CONF_METAL_SERVER_PATH))
        .setOptional(true);

    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
        .addStore(fileConfigStoreOptions);
    ConfigRetriever retriever = ConfigRetriever.create(getVertx(), retrieverOptions);
    retriever.getConfig().compose((JsonObject conf) -> {
      JsonObject mongoConf = conf.getJsonObject(MONGO_CONF);
      if (mongoConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", MONGO_CONF, CONF_METAL_SERVER_PATH));
      }

      mongo = MongoClient.createShared(
          getVertx(),
          mongoConf
      );

      JsonObject projectConf = conf.getJsonObject(PROJECT_CONF);
      if (projectConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", PROJECT_CONF, CONF_METAL_SERVER_PATH));
      }

      JsonObject projectServiceConf = projectConf.getJsonObject(PROJECT_SERVICE_CONF);
      if (projectServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", PROJECT_SERVICE_CONF, CONF_METAL_SERVER_PATH + "." + PROJECT_CONF));
      }

      String address = projectServiceConf.getString(PROJECT_SERVICE_ADDRESS_CONF);
      if (address == null || address.isBlank()) {
        return Future.failedFuture(String.format("%s is not configured in %s.",
            PROJECT_SERVICE_ADDRESS_CONF, CONF_METAL_SERVER_PATH + "." + PROJECT_CONF + "." + PROJECT_SERVICE_CONF));
      }

      workerExecutor = vertx.createSharedWorkerExecutor("project-worker-executor", 1);
      provider = IProjectService.createProvider(getVertx(), mongo, workerExecutor, projectServiceConf);
      binder = new ServiceBinder(getVertx());
      binder.setAddress(address);
      consumer = binder.register(IProjectService.class, provider);
      return Future.succeededFuture();
    }).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure((Throwable error) -> {
      startPromise.fail(error);
    });
  }

  public static RestApi createRestApi(Vertx vertx, String provider) {
    return new RestApi(vertx, provider);
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    binder.unregister(consumer);
    mongo.close().onSuccess(ret -> {
      stopPromise.complete();
    }).onFailure(error -> {
      stopPromise.fail(error);
    });
  }

  public static class RestApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(RestApi.class);
    private IProjectService service;

    private RestApi(Vertx vertx, String provider) {
      service = IProjectService.create(vertx, new JsonObject().put("address", provider));
    }

    public void add(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String name = body.getString("name");

      JsonArray pkgs = body.getJsonArray("pkgs");
      JsonObject platform = body.getJsonObject("platform");
      JsonArray backendArgs = body.getJsonArray("backendArgs");
      JsonObject spec = body.getJsonObject("spec");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found projectName in request.", 400)) {
        return;
      }

      List<String> pkgList = JsonConvertor.jsonArrayToList(pkgs);
      List<String> backendArgList = JsonConvertor.jsonArrayToList(backendArgs);

      Future<String> result = service.createProject(userId, name, pkgList, platform, backendArgList, spec);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void copy(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String name = body.getString("name");
      String copyName = body.getString("copyName");
      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found projectName in request.", 400)) {
        return;
      }

      Future<String> result;
      if (copyName == null || copyName.isBlank()) {
        result = service.createProjectFrom(userId, name);
      } else {
        result = service.createProjectFromWithCopyName(userId, name, copyName);
      }
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getOfName(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");
      if (
        OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found name in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.getOfName(userId, name);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getSpecOfName(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");
      if (
          OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found name in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.getSpecOfName(userId, name);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getSpecSchemaOfMetalId(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");
      String metalId = ctx.request().params().get("metalId");

      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found name in request.", 400)) {
        return;
      }

      if (OnFailure.doTry(ctx, ()->{return metalId == null || metalId.isBlank();}, "Fail to found name in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.getSpecSchemaOfMetalId(deployId, metalId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getBackendServiceStatusOfDeployId(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");
      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found name in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.getBackendServiceStatusOfDeployId(deployId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void heartOfDeployId(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");
      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.heartOfDeployId(deployId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      Future<List<JsonObject>> result = service.getAllOfUser(userId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getAll(RoutingContext ctx) {
      Future<List<JsonObject>> result = service.getAll();
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void updateName(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found project name in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      String newName = body.getString("newName");
      if (OnFailure.doTry(ctx, ()->{return newName == null || newName.isBlank();}, "Fail to found new project name in request.", 400)) {
        return;
      }

      service.updateName(userId, name, newName)
          .onSuccess((JsonObject project)-> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", project);
            SendJson.send(ctx, resp, 200);
          }).onFailure((Throwable error) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL")
                .put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
            LOGGER.error(error);
          });
    }

    public void updateDeployConfs(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String deployId = ctx.request().params().get("deployId");
      String path = ctx.request().path();
      JsonObject body = ctx.body().asJsonObject();
      JsonObject updateConfs = body.getJsonObject("updateConfs");
      if (OnFailure.doTry(ctx, ()->{return updateConfs == null || updateConfs.isEmpty();}, "Fail to found legal updateConfs in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.updateDeployConfsByPath(deployId, updateConfs);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void updateSpec(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found project name in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonObject spec = body.getJsonObject("spec");
      if (OnFailure.doTry(ctx, ()->{return spec == null || spec.isEmpty();}, "Fail to found legal spec in request.", 400)) {
        return;
      }

      try {
        SpecJson.check(spec);
      } catch (IllegalArgumentException e) {
        OnFailure.doTry(ctx, ()->{return true;}, e.getLocalizedMessage(), 400);
        return;
      }

      service.updateSpec(userId, name, spec)
          .onSuccess((JsonObject ret) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", ret);
            SendJson.send(ctx, resp, 200);
          })
          .onFailure((Throwable error) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL")
                .put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
            LOGGER.error(error);
          });
    }

    public void updatePlatform(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String deployId = ctx.request().params().get("deployId");

      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonObject platform = body.getJsonObject("platform");
      if (OnFailure.doTry(ctx, ()->{return platform == null || platform.isEmpty();}, "Fail to found legal platform in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.updatePlatform(deployId, platform);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void forceKillBackend(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");
      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }
      Future<JsonObject> result = service.forceKillBackend(deployId);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void updateBackendArgs(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");

      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonArray backendArgs = body.getJsonArray("args");

      if (OnFailure.doTry(ctx, ()->{return backendArgs == null;}, "Fail to found legal backend arguments in request.", 400)) {
        return;
      }
      List<String> backendArgList = JsonConvertor.jsonArrayToList(backendArgs);
      Future<JsonObject> result = service.updateBackendArgs(deployId, backendArgList);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void updateBackendStatus(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");

      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonObject status = body.getJsonObject("status");

      if (OnFailure.doTry(ctx, ()->{return status == null || status.isEmpty();}, "Fail to found legal backend arguments in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.updateBackendStatus(deployId, status);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void getBackendStatus(RoutingContext ctx) {
      String deployId = ctx.request().params().get("deployId");

      if (OnFailure.doTry(ctx, ()->{return deployId == null || deployId.isBlank();}, "Fail to found deploy id in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.getBackendStatusOfDeployId(deployId);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void remove(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");
      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found project name in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.removeOfName(userId, name);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void removeAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      Future<JsonObject> result = service.removeAllOfUser(userId);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void removeAll(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      Future<JsonObject> result = service.removeAll();
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void deploy(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found project name in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.deploy(userId, name);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void reDeploy(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found project name in request.", 400)) {
        return;
      }

      Future<JsonObject> result = service.reDeploy(userId, name);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void analysis(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found name in request.", 400)) {
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonObject spec = body.getJsonObject("spec");
      if (OnFailure.doTry(ctx, ()->{return spec == null || spec.isEmpty();}, "Fail to found legal spec in request.", 400)) {
        return;
      }

      try {
        SpecJson.check(spec);
      } catch (IllegalArgumentException e) {
        OnFailure.doTry(ctx, ()->{return true;}, e.getLocalizedMessage(), 400);
        return;
      }

      Future<JsonObject> result = service.analysis(userId, name, spec);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void analysisCurrent(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String name = ctx.request().params().get("name");

      if (OnFailure.doTry(ctx, ()->{return name == null || name.isBlank();}, "Fail to found name in request.", 400)) {
        return;
      }

      service.getSpecOfName(userId, name).compose((JsonObject spec) -> {
        try {
          SpecJson.check(spec);
        } catch (IllegalArgumentException e) {
          return Future.failedFuture(e);
        }
        return Future.<JsonObject>succeededFuture(spec);
      }).onFailure(error -> {
        OnFailure.doTry(ctx, ()->{return true;}, error.getLocalizedMessage(), 400);
      }).onSuccess((JsonObject spec) -> {
        Future<JsonObject> result = service.analysis(userId, name, spec);
        RestServiceEnd.end(ctx, result, LOGGER);
      });
    }
  }

}
