package org.metal.server.exec;

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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.List;
import org.metal.server.project.Project;
import org.metal.server.project.Project.RestApi;
import org.metal.server.util.OnFailure;
import org.metal.server.util.RestServiceEnd;

public class Exec extends AbstractVerticle {
  public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
  public static final String MONGO_CONF = "mongoConf";
  public static final String EXEC_SERVICE_CONF = "execService";
  public static final String EXEC_SERVICE_ADDRESS_CONF = "address";
  public static final String EXEC_CONF = "exec";
  private MongoClient mongo;
  private ExecService execService;
  private MessageConsumer<JsonObject> consumer;

  private Exec() {}
  public static Exec create() {
    return new Exec();
  }

  public static RestApi createRestApi(Vertx vertx, String provider) {
    return new RestApi(vertx, provider);
  }

  public static class RestApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(Exec.RestApi.class);
    private ExecService service;

    private RestApi(Vertx vertx, String provider) {
      service = ExecService.create(vertx, new JsonObject().put("address", provider));
    }

    public void getOfId(RoutingContext ctx) {
      String execId = ctx.request().params().get("execId");
      if (
          OnFailure.doTry(ctx, ()->{return execId == null || execId.isBlank();}, "Fail to found exec id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.getOfId(execId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getOfIdNoDetail(RoutingContext ctx) {
      String execId = ctx.request().params().get("execId");
      if (
          OnFailure.doTry(ctx, ()->{return execId == null || execId.isBlank();}, "Fail to found exec id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.getOfIdNoDetail(execId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void getAll(RoutingContext ctx) {
      Future<List<JsonObject>> result = service.getAll();
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void getAllNoDetail(RoutingContext ctx) {
      Future<List<JsonObject>> result = service.getAllNoDetail();
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void getAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      Future<List<JsonObject>> result = service.getAllOfUser(userId);
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }



    public void getAllOfUserNoDetail(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      Future<List<JsonObject>> result = service.getAllOfUserNoDetail(userId);
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void getAllOfProject(RoutingContext ctx) {
      String projectId = ctx.request().params().get("projectId");
      if (
          OnFailure.doTry(ctx, ()->{return projectId == null || projectId.isBlank();}, "Fail to found project id in request.", 400)
      ) {
        return;
      }
      Future<List<JsonObject>> result = service.getAllOfProject(projectId);
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void getAllOfProjectNoDetail(RoutingContext ctx) {
      String projectId = ctx.request().params().get("projectId");
      if (
          OnFailure.doTry(ctx, ()->{return projectId == null || projectId.isBlank();}, "Fail to found project id in request.", 400)
      ) {
        return;
      }
      Future<List<JsonObject>> result = service.getAllOfProjectNoDetail(projectId);
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }

    public void remove(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String execId = ctx.request().params().get("execId");
      if (
          OnFailure.doTry(ctx, ()->{return execId == null || execId.isBlank();}, "Fail to found exec id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.remove(userId, execId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void forceRemove(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String execId = ctx.request().params().get("execId");
      if (
          OnFailure.doTry(ctx, ()->{return execId == null || execId.isBlank();}, "Fail to found exec id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.forceRemove(userId, execId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void removeAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      Future<JsonObject> result = service.removeAllOfUser(userId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void forceRemoveAllOfUser(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");

      Future<JsonObject> result = service.forceRemoveAllOfUser(userId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void removeAllOfProject(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectId = ctx.request().params().get("projectId");
      if (
          OnFailure.doTry(ctx, ()->{return projectId == null || projectId.isBlank();}, "Fail to found project id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.removeAllOfProject(userId, projectId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void forceRemoveAllOfProject(RoutingContext ctx) {
      User user = ctx.user();
      String userId = user.get("_id");
      String projectId = ctx.request().params().get("projectId");
      if (
          OnFailure.doTry(ctx, ()->{return projectId == null || projectId.isBlank();}, "Fail to found project id in request.", 400)
      ) {
        return;
      }

      Future<JsonObject> result = service.forceRemoveAllOfProject(userId, projectId);
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void removeAll(RoutingContext ctx) {
      Future<JsonObject> result = service.removeAll();
      RestServiceEnd.end(ctx, result, LOGGER);
    }

    public void forceRemoveAll(RoutingContext ctx) {
      Future<JsonObject> result = service.forceRemoveAll();
      RestServiceEnd.end(ctx, result, LOGGER);
    }

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
      JsonObject execConf = conf.getJsonObject(EXEC_CONF);
      JsonObject execServiceConf = execConf.getJsonObject(EXEC_SERVICE_CONF);
      String execServiceAddress = execServiceConf.getString(EXEC_SERVICE_ADDRESS_CONF);
      if (mongoConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", MONGO_CONF, CONF_METAL_SERVER_PATH));
      }
      if (execServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", EXEC_SERVICE_CONF, CONF_METAL_SERVER_PATH));
      }
      if (execServiceAddress == null || execServiceAddress.isBlank()) {
        return Future.failedFuture(String.format("%s is not configured in %s.", EXEC_SERVICE_ADDRESS_CONF, CONF_METAL_SERVER_PATH + "." + EXEC_SERVICE_CONF));
      }

      this.mongo = MongoClient.createShared(getVertx(), mongoConf);
      this.execService = ExecService.createProvider(getVertx(), mongo, execServiceConf);
      ServiceBinder binder = new ServiceBinder(getVertx());
      binder.setAddress(execServiceAddress);
      this.consumer = binder.register(ExecService.class, this.execService);
      return Future.succeededFuture();
    }).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure(error -> {
      startPromise.fail(error);
    });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    this.consumer.unregister().compose(ret -> {
      return mongo.close();
    }).onSuccess(ret -> {
      stopPromise.complete();
    }).onFailure(error -> {
      stopPromise.fail(error);
    });
  }
}
