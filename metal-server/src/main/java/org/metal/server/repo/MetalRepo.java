package org.metal.server.repo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;
import org.metal.server.repo.service.IMetalRepoService;

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

  private MetalRepo() {}

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
        return Future.failedFuture(String.format("%s is not configured in %s.", MONGO_CONF, CONF_METAL_SERVER_PATH));
      }

      mongo = MongoClient.createShared(
          getVertx(),
          mongoConf
      );

      JsonObject metalRepoConf = conf.getJsonObject(METAL_REPO_CONF);
      if (metalRepoConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", METAL_REPO_CONF, CONF_METAL_SERVER_PATH));
      }

      JsonObject metalRepoServiceConf = metalRepoConf.getJsonObject(METAL_REPO_SERVICE_CONF);
      if (metalRepoServiceConf == null) {
        return Future.failedFuture(String.format("%s is not configured in %s.", METAL_REPO_CONF, CONF_METAL_SERVER_PATH + "." + METAL_REPO_CONF));
      }

      String address = metalRepoServiceConf.getString(METAL_REPO_SERVICE_ADDRESS_CONF);
      if (address == null || address.isBlank()) {
        return Future.failedFuture(String.format("%s is not configured in %s.",
            METAL_REPO_SERVICE_ADDRESS_CONF, CONF_METAL_SERVER_PATH + "." + METAL_REPO_CONF + "." + METAL_REPO_SERVICE_CONF));
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
}
