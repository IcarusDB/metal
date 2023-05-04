package org.metal.server.repo;

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
import org.metal.server.repo.service.IMetalRepoService;
import org.metal.server.util.JsonConvertor;
import org.metal.server.util.OnFailure;
import org.metal.server.util.RestServiceEnd;
import org.metal.server.util.SendJson;

public class MetalRepo extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(MetalRepo.class);
  public static final String CONF_METAL_SERVER_PATH = "conf/metal-server.json";
  public static final String MONGO_CONF = "mongoConf";
  public static final String METAL_REPO_CONF = "metalRepo";
  public static final String METAL_REPO_SERVICE_CONF = "metalRepoService";
  public static final String METAL_REPO_SERVICE_ADDRESS_CONF = "address";

  private MongoClient mongo;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private IMetalRepoService provider;

  private MetalRepo() {
  }

  public static MetalRepo create() {
    return new MetalRepo();
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
        return Future.failedFuture(
            String.format("%s is not configured in %s.", MONGO_CONF, CONF_METAL_SERVER_PATH));
      }

      mongo = MongoClient.createShared(
          getVertx(),
          mongoConf
      );

      JsonObject metalRepoConf = conf.getJsonObject(METAL_REPO_CONF);
      if (metalRepoConf == null) {
        return Future.failedFuture(
            String.format("%s is not configured in %s.", METAL_REPO_CONF, CONF_METAL_SERVER_PATH));
      }

      JsonObject metalRepoServiceConf = metalRepoConf.getJsonObject(METAL_REPO_SERVICE_CONF);
      if (metalRepoServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", METAL_REPO_CONF,
            CONF_METAL_SERVER_PATH + "." + METAL_REPO_CONF));
      }

      String address = metalRepoServiceConf.getString(METAL_REPO_SERVICE_ADDRESS_CONF);
      if (address == null || address.isBlank()) {
        return Future.failedFuture(String.format("%s is not configured in %s.",
            METAL_REPO_SERVICE_ADDRESS_CONF,
            CONF_METAL_SERVER_PATH + "." + METAL_REPO_CONF + "." + METAL_REPO_SERVICE_CONF));
      }
      provider = IMetalRepoService.createProvider(getVertx(), mongo, metalRepoServiceConf);
      binder = new ServiceBinder(getVertx());
      binder.setAddress(address);
      consumer = binder.register(IMetalRepoService.class, provider);
      return Future.succeededFuture();
    }).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure(error -> {
      startPromise.fail(error);
    });
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

  public static RestApi createRestApi(Vertx vertx, String provider) {
    return new RestApi(vertx, provider);
  }

  public static class RestApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestApi.class);
    private IMetalRepoService metalRepoService;

    private RestApi(Vertx vertx, String provider) {
      metalRepoService = IMetalRepoService.create(
          vertx,
          new JsonObject()
              .put("address", provider)
      );
    }

    public void add(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String type = body.getString("type");
      String scope = body.getString("scope");
      JsonObject metal = body.getJsonObject("metal");
      boolean isFailed = false;
      isFailed = OnFailure.doTry(ctx,
          () -> {
            return type == null || type.isBlank();
          },
          "Fail to found type in request.",
          400
      );
      if (isFailed) {
        return;
      }

      isFailed = OnFailure.doTry(ctx,
          () -> {
            return scope == null || scope.isBlank();
          },
          "Fail to found scope in request.",
          400
      );
      if (isFailed) {
        return;
      }

      isFailed = OnFailure.doTry(ctx,
          () -> {
            return metal == null || metal.isEmpty();
          },
          "Fail to found metal in request.",
          400
      );
      if (isFailed) {
        return;
      }

      metalRepoService.add(userId, type, scope, metal)
          .onSuccess((String metalId) -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", new JsonObject().put("id", metalId));
            SendJson.send(ctx, resp, 201);
          })
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            if (error instanceof IllegalArgumentException) {
              SendJson.send(ctx, resp, 400);
            } else {
              SendJson.send(ctx, resp, 500);
            }
          });
    }

    public void getAllOfUser(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");

      metalRepoService.getAllOfUser(userId).onSuccess(metals -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "OK");
        resp.put("data", metals);
        SendJson.send(ctx, resp, 200);
      }).onFailure(error -> {
        JsonObject resp = new JsonObject();
        resp.put("status", "FAIL");
        resp.put("msg", error.getLocalizedMessage());
        SendJson.send(ctx, resp, 500);
      });
    }

    public void getAllOfUserScope(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String scope = ctx.request().params().get("scope");

      if (OnFailure.doTry(ctx,
          () -> {
            return scope == null || scope.isBlank();
          },
          "Fail to found scope field in request.",
          400
      )) {
        return;
      }

      metalRepoService.getAllOfUserScope(userId, scope)
          .onSuccess(metals -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", metals);
            SendJson.send(ctx, resp, 200);
          })
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          });
    }

    public void getAllOfPublic(RoutingContext ctx) {
      metalRepoService.getAllOfPublic()
          .onSuccess(metals -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", metals);
            SendJson.send(ctx, resp, 200);
          })
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("mgs", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          });
    }

    public void getAllOfPkg(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String groupId = ctx.request().params().get("groupId");
      String artifactId = ctx.request().params().get("artifactId");
      String version = ctx.request().params().get("version");

      if (
          OnFailure.doTry(ctx, () -> {
            return groupId == null || groupId.isBlank();
          }, "Fail to found groupId in request.", 400)
      ) {
        return;
      }

      metalRepoService.getAllOfPkg(
              userId,
              groupId, artifactId, version)
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL")
                .put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          })
          .onSuccess(metals -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", metals);
            SendJson.send(ctx, resp, 200);
          });
    }

    public void getAllOfType(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String metalType = ctx.request().params().get("metalType");
      if (
          OnFailure.doTry(ctx, () -> {
            return metalType == null || metalType.isBlank();
          }, "Fail to found metalType in request.", 400)
      ) {
        return;
      }

      metalRepoService.getAllOfType(userId, metalType)
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL")
                .put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          })
          .onSuccess(metals -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK")
                .put("data", metals);
            SendJson.send(ctx, resp, 200);
          });
    }

    public void getOfClass(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String clazz = ctx.request().params().get("class");

      if (OnFailure.doTry(ctx, () -> {
        return clazz == null || clazz.isBlank();
      }, "The class field in request is invalid", 400)) {
        return;
      }

      Future<JsonObject> result = metalRepoService.getOfClass(userId, clazz);
      RestServiceEnd.<JsonObject>end(ctx, result, LOGGER);
    }

    public void getAllOfClasses(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      List<String> classes = null;
      try {
        JsonArray classArray = body.getJsonArray("classes");
        classes = JsonConvertor.jsonArrayToList(classArray);
      } catch (ClassCastException e) {
        if (OnFailure.doTry(ctx, () -> {
          return true;
        }, "The classes field in request is invalid", 400)) {
          return;
        }
      }

      Future<List<JsonObject>> result = metalRepoService.getAllOfClasses(userId, classes);
      RestServiceEnd.<List<JsonObject>>end(ctx, result, LOGGER);
    }


    public void addFromManifest(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String scope = ctx.request().params().get("scope");
      JsonObject manifest = body.getJsonObject("manifest");

      if (OnFailure.doTry(ctx,
          () -> {
            return scope == null || scope.isBlank();
          },
          "Fail to found scope field in request.",
          400
      )) {
        return;
      }

      if (OnFailure.doTry(ctx,
          () -> {
            return manifest == null || manifest.isEmpty();
          },
          "Fail to found manifest field in request.",
          400
      )) {
        return;
      }

      metalRepoService.addFromManifest(userId, scope, manifest)
          .onSuccess(ret -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", ret);
            SendJson.send(ctx, resp, 201);
          })
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          });
    }

    public void removePrivate(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");
      String metalId = ctx.request().params().get("metalId");

      if (OnFailure.doTry(ctx,
          () -> {
            return metalId == null || metalId.isBlank();
          },
          "Fail to found metalId field in request.",
          400
      )) {
        return;
      }

      metalRepoService.removePrivate(userId, metalId)
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          })
          .onSuccess(ret -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", ret);
            SendJson.send(ctx, resp, 200);
          });
    }

    public void removeAllPrivateOfUser(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();
      User user = ctx.user();
      String userId = user.get("_id");

      metalRepoService.removeAllPrivateOfUser(userId)
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          })
          .onSuccess(ret -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", ret);
            SendJson.send(ctx, resp, 200);
          });
    }

    public void removeAll(RoutingContext ctx) {
      metalRepoService.removeAll()
          .onFailure(error -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "FAIL");
            resp.put("msg", error.getLocalizedMessage());
            SendJson.send(ctx, resp, 500);
          })
          .onSuccess(ret -> {
            JsonObject resp = new JsonObject();
            resp.put("status", "OK");
            resp.put("data", ret);
            SendJson.send(ctx, resp, 200);
          });
    }


  }
}
