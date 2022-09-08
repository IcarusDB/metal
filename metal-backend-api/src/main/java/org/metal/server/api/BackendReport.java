package org.metal.server.api;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface BackendReport {
  public static BackendReport create(Vertx vertx, JsonObject config) {
    String address = config.getString("address");
    return new BackendReportVertxEBProxy(vertx, address);
  }

  public Future<Void> reportCreate(JsonObject create);
  
  public Future<Void> reportLiveness(JsonObject liveness);
  
  public Future<Void> reportFinish(JsonObject finish);
  
  public Future<Void> reportFailure(JsonObject failure);
  
  
}
