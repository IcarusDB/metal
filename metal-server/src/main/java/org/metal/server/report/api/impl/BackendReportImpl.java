package org.metal.server.report.api.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.metal.server.api.BackendReport;

public class BackendReportImpl implements BackendReport {
  private MongoClient mongo;

  @Override
  public Future<Void> reportCreate(JsonObject create) {

    return null;
  }

  @Override
  public Future<Void> reportLiveness(JsonObject liveness) {
    return null;
  }

  @Override
  public Future<Void> reportFinish(JsonObject finish) {
    return null;
  }

  @Override
  public Future<Void> reportFailure(JsonObject failure) {
    return null;
  }
}
