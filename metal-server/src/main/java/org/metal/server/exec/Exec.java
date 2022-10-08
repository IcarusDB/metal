package org.metal.server.exec;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

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
