package org.metal.server.report.api.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.metal.server.api.BackendReport;
import org.metal.server.api.ExecState;
import org.metal.server.exec.ExecService;

public class BackendReportImpl implements BackendReport {
  private MongoClient mongo;
  private ExecService execService;

  @Override
  public Future<Void> reportExecSubmit(JsonObject submit) {
    if (!ExecState.SUBMIT.toString().equals(submit.getString("status"))) {
      return Future.failedFuture(
          String.format("The parameter create:%s is not in \'%s\' status.", submit.toString(), ExecState.SUBMIT.toString())
      );
    }

    if (submit.getString("id") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost id.", submit.toString())
      );
    }

    if (submit.getLong("submitTime") == null) {
      return Future.failedFuture(
        String.format("The parameter create:%s lost submitTime.", submit.toString())
      );
    }

    if (submit.getString("deployId") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost deployId.", submit.toString())
      );
    }

    if (submit.getInteger("epoch") == null) {
      return Future.failedFuture(
          String.format("The parameter create:%s lost epoch.", submit.toString())
      );
    }

    String execId = submit.getString("id");
    String deployId = submit.getString("deployId");
    int epoch = submit.getInteger("epoch");
    long submitTime = submit.getLong("submitTime");

    return execService.getStatus(execId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          if (epoch != lastEpoch) {
            String msg = String.format("epoch: %d in %s is not equal the last epoch in metal server.[%d != %d]. %s-%d maybe have been evicted.",
                epoch,
                submit.toString(),
                epoch, lastEpoch,
                deployId, epoch
            );
            return Future.failedFuture(msg);
          } else {
            return Future.succeededFuture(lastStatus);
          }
        }).compose((JsonObject lastStatus) -> {
          ExecState lastState = ExecState.valueOf(lastStatus.getString("status"));
          if (!lastState.equals(ExecState.CREATE)) {
            String msg = String.format("The status of exec can\'t switch from %s to %s.", lastState.toString(), ExecState.SUBMIT.toString());
            return Future.failedFuture(msg);
          } else {
            return Future.succeededFuture(lastStatus);
          }
        }).compose((JsonObject lastStatus) -> {
          JsonObject update = new JsonObject();
          update.put("status", ExecState.SUBMIT.toString())
              .put("submitTime", submitTime);
          return execService.updateStatus(execId, update);
        });
  }

  @Override
  public Future<Void> reportExecRunning(JsonObject running) {
    return null;
  }

  @Override
  public Future<Void> reportExecFinish(JsonObject finish) {
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
  public Future<Void> reportExecFailure(JsonObject failure) {
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

  @Override
  public Future<Void> reportBackendUp(JsonObject up) {
    return null;
  }

  @Override
  public Future<Void> reportBackendDown(JsonObject down) {
    return null;
  }

  @Override
  public Future<Void> reportBackendFailure(JsonObject failure) {
    return null;
  }
}
