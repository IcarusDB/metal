package org.metal.server.project;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.impl.MappingStream;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.List;
import java.util.UUID;
import org.metal.server.SendJson;
import org.metal.server.project.service.IProjectService;

public class Project extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(Project.class);

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
    String mongoConnection = config().getString("mongoConnection");
    mongo = MongoClient.createShared(
        getVertx(),
        new JsonObject().put("connection_string", mongoConnection)
    );

    provider = IProjectService.createProvider(getVertx(), mongo);
    binder = new ServiceBinder(getVertx());
    String address = config().getString("projectAddress");
    binder.setAddress(address);
    consumer = binder.register(IProjectService.class, provider);

    startPromise.complete();
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
            error.printStackTrace();
          });
    }
  }

}
