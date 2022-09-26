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

  public Future<Void> reportExecSubmit(JsonObject create);
  
  public Future<Void> reportExecRunning(JsonObject running);
  
  public Future<Void> reportExecFinish(JsonObject finish);
  
  public Future<Void> reportExecFailure(JsonObject failure);

  public Future<Void> reportBackendUp(JsonObject up);

  public Future<Void> reportBackendDown(JsonObject down);

  public Future<Void> reportBackendFailure(JsonObject failure);

}
