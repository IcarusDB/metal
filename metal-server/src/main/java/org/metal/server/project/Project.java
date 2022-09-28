package org.metal.server.project;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
import org.metal.server.SendJson;
import org.metal.server.project.service.IProjectService;

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

      provider = IProjectService.createProvider(getVertx(), mongo, projectServiceConf);
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
    mongo.close();
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
      String projectName = body.getString("projectName");
      String platform = body.getString("platform");

      if (projectName == null || projectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", "Fail to found projectName in request.");
        SendJson.send(ctx, resp, 400);
        return ;
      }

      if (platform == null) {
        service.createEmptyProject(userId, projectName)
            .onSuccess((String projectId) -> {
              JsonObject resp = new JsonObject();
              resp.put("status", "OK");
              resp.put("data", new JsonObject().put("id", projectId));
              SendJson.send(ctx, resp, 201);
            })
            .onFailure((Throwable error) -> {
              JsonObject resp = new JsonObject();
              resp.put("status", "FAIL");
              resp.put("msg", error.getLocalizedMessage());
              SendJson.send(ctx, resp, 500);
              LOGGER.error(error);
            });
        return;
      }

      try {
        Platform.valueOf(platform);
      } catch (IllegalArgumentException e) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", e.getLocalizedMessage());
        SendJson.send(ctx, resp, 400);
        return;
      }

      JsonObject platformArgs = body.getJsonObject("platformArgs", new JsonObject());
      JsonObject backendArgs = body.getJsonObject("backendArgs", new JsonObject());
      JsonObject spec = body.getJsonObject("spec", new JsonObject());

      service.createProject(
          userId,
          projectName,
          platform,
          platformArgs,
          backendArgs,
          spec
      ).onSuccess((String projectId) -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "OK")
            .put("data", new JsonObject().put("id", projectId));
        SendJson.send(ctx, resp, 201);
      }).onFailure((Throwable error) -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", error.getLocalizedMessage());
        SendJson.send(ctx, resp, 400);
      });
    }

    public void get(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectName = ctx.request().params().get("projectName");
      if (projectName == null || projectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", "projectName is not set.");
        SendJson.send(ctx, resp, 404);
        return;
      }

      service.getOfName(userId, projectName)
          .onSuccess((JsonObject project) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", project);
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

    public void getAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      service.getAllOfUser(userId)
          .onSuccess((List<JsonObject> projects) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", JsonArray.of(projects.toArray()));
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

    public void getAll(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      service.getAll()
          .onSuccess((List<JsonObject> projects) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", JsonArray.of(projects.toArray()));
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

    public void updateName(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectName = ctx.request().params().get("projectName");
      if (projectName == null || projectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", "projectName is not set.");
        SendJson.send(ctx, resp, 404);
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      String newProjectName = body.getString("newProjectName");
      if (newProjectName == null || newProjectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", "newProjectName is not set.");
        SendJson.send(ctx, resp, 400);
        return;
      }

      service.updateName(userId, projectName, newProjectName)
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

    public void updatePath(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectName = ctx.request().params().get("projectName");
      String path = ctx.request().path();
      JsonObject body = ctx.body().asJsonObject();
      JsonObject update = body.getJsonObject("update");
      if (update == null || update.isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", "update is not set.");
        SendJson.send(ctx, resp, 400);
        return;
      }

      service.updateByPath(
          userId,
          projectName,
          update
      ).onSuccess((JsonObject ret) -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "OK")
            .put("data", ret);
        SendJson.send(ctx, resp, 200);
      }).onFailure((Throwable error) -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", error.getLocalizedMessage());
        SendJson.send(ctx, resp, 500);
        LOGGER.error(error);
      });
    }

    public void updateSpec(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectName = ctx.request().params().get("projectName");
      if (projectName == null || projectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", "projectName is not set.");
        SendJson.send(ctx, resp, 404);
        return;
      }

      JsonObject body = ctx.body().asJsonObject();
      JsonObject spec = body.getJsonObject("spec");
      if (spec == null || spec.isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", "spec is not set.");
        SendJson.send(ctx, resp, 400);
        return;
      }

      service.updateSpec(userId, projectName, spec)
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

    public void remove(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectName = ctx.request().params().get("projectName");
      if (projectName == null || projectName.strip().isEmpty()) {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL")
            .put("msg", "projectName is not set.");
        SendJson.send(ctx, resp, 404);
        return;
      }

      service.removeOfName(userId, projectName)
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

    public void removeAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      service.removeAllOfUser(userId)
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

    public void removeAll(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      service.removeAll()
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
  }

}
