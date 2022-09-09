package org.metal.backend.api;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface BackendService {

  public static BackendService create(Vertx vertx, JsonObject config) {
    String address = config.getString("address");
    return new BackendServiceVertxEBProxy(vertx, address);
  }

  public Future<JsonObject> analyse(JsonObject spec);

  public Future<JsonObject> schemaAPI(String metalId);

  public Future<JsonObject> heartAPI();

  public Future<JsonObject> statusAPI();

  public Future<Void> execAPI(JsonObject exec);
}
