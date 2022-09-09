package org.metal.server.report.api.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.metal.server.api.BackendReport;

public class BackendReportImpl implements BackendReport {
  private MongoClient mongo;

  @Override
  public Future<Void> reportCreate(JsonObject create) {
    if (!"OK".equals(create.getString("status"))) {
      return Future.failedFuture(
          String.format("The parameter create:%s is not in \'OK\' status.", create.toString())
      );
    }

    if (create.getString("id") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost id.", create.toString())
      );
    }

    if (create.getLong("submitTime") == null) {
      return Future.failedFuture(
        String.format("The parameter create:%s lost submitTime.", create.toString())
      );
    }

    return mongo.findOneAndUpdate(
        "exec",
        new JsonObject().put("id", create.getString("id")),
        create
        ).compose((JsonObject ret) -> {
          return Future.succeededFuture();
    });
  }

  @Override
  public Future<Void> reportLiveness(JsonObject liveness) {
    return null;
  }

  @Override
  public Future<Void> reportFinish(JsonObject finish) {
    if (!"FINISH".equals(finish.getString("status"))) {
      return Future.failedFuture(
          String.format("The parameter create:%s is not in \'FINISH\' status.", finish.toString())
      );
    }

    if (finish.getString("id") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost id.", finish.toString())
      );
    }

    if (finish.getLong("finishTime") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost finishTime.", finish.toString())
      );
    }

    return mongo.findOneAndUpdate(
        "exec",
        new JsonObject().put("id", finish.getString("id")),
        finish
    ).compose((JsonObject ret) -> {
      return Future.succeededFuture();
    });
  }

  @Override
  public Future<Void> reportFailure(JsonObject failure) {
    if (!"FINISH".equals(failure.getString("status"))) {
      return Future.failedFuture(
          String.format("The parameter create:%s is not in \'FINISH\' status.", failure.toString())
      );
    }

    if (failure.getString("id") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost id.", failure.toString())
      );
    }

    if (failure.getLong("finishTime") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost finishTime.", failure.toString())
      );
    }

    return mongo.findOneAndUpdate(
        "exec",
        new JsonObject().put("id", failure.getString("id")),
        failure
    ).compose((JsonObject ret) -> {
      return Future.succeededFuture();
    });
  }
}
