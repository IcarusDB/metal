package org.metal.server.report.api.impl;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.metal.server.api.BackendReportService;
import org.metal.server.api.BackendState;
import org.metal.server.api.ExecState;
import org.metal.server.exec.ExecService;
import org.metal.server.project.ProjectDB;
import org.metal.server.project.service.IProjectService;

public class BackendReportServiceImpl implements BackendReportService {
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendReportServiceImpl.class);

  private ExecService execService;
  private IProjectService projectService;

  public BackendReportServiceImpl(ExecService execService,
      IProjectService projectService) {
    this.execService = execService;
    this.projectService = projectService;
  }

  @Override
  public Future<Void> reportExecSubmit(JsonObject submit) {
    String timeName = "submitTime";
    try {
      checkExecStatus(submit, ExecState.SUBMIT);
      checkExecReport(submit);
      checkTime(submit, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String execId = submit.getString("id");
    String deployId = submit.getString("deployId");
    int epoch = submit.getInteger("epoch");
    long submitTime = submit.getLong(timeName);

    return execService.getStatus(execId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          ExecState lastState = ExecState.valueOf(lastStatus.getString("status"));
          if (!lastState.equals(ExecState.CREATE)) {
            String msg = String.format("The status of exec can\'t switch from %s to %s.", lastState.toString(), ExecState.SUBMIT.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", ExecState.SUBMIT.toString())
              .put(timeName, submitTime);
          return execService.updateStatus(execId, update);
        });
  }

  private static boolean checkBackendStatus(JsonObject report, BackendState expect) throws IllegalArgumentException {
    if (!report.containsKey("status")) {
      throw new IllegalArgumentException(String.format("status is lost in %s.", report.toString()));
    }

    BackendState backendState = BackendState.valueOf(report.getString("status"));
    if (!expect.equals(backendState)) {
      throw new IllegalArgumentException(String.format("The parameter %s is not in '%s' status.", report.toString(), expect.toString()));
    }
    return true;
  }

  private static boolean checkBackendReport(JsonObject report) throws IllegalArgumentException {
    if (!report.containsKey("deployId")) {
      throw new IllegalArgumentException(
          String.format("The parameter %s lost deployId.", report.toString())
      );
    }

    if (!report.containsKey("epoch")) {
      throw new IllegalArgumentException(
          String.format("The parameter %s lost epoch.", report.toString())
      );
    }

    try {
      report.getInteger("epoch");
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return true;
  }

  private static boolean checkExecStatus(JsonObject report, ExecState expect) throws IllegalArgumentException {
    if (!report.containsKey("status")) {
      throw new IllegalArgumentException(String.format("status is lost in %s.", report.toString()));
    }

    ExecState execState = ExecState.valueOf(report.getString("status"));
    if (!expect.equals(execState)) {
      throw new IllegalArgumentException(String.format("The parameter %s is not in '%s' status.", report.toString(), expect.toString()));
    }
    return true;
  }

  private static boolean checkExecReport(JsonObject report) throws IllegalArgumentException {
    if (!report.containsKey("id")) {
      throw new IllegalArgumentException(String.format("The parameter %s lost id.", report.toString()));
    }

    if (!report.containsKey("deployId")) {
      throw new IllegalArgumentException(
              String.format("The parameter %s lost deployId.", report.toString())
      );
    }

    if (!report.containsKey("epoch")) {
      throw new IllegalArgumentException(
              String.format("The parameter %s lost epoch.", report.toString())
      );
    }

    try {
      report.getInteger("epoch");
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }

    return true;
  }

  private static boolean checkTime(JsonObject report, String timeName) {
    if (!report.containsKey(timeName)) {
      throw new IllegalArgumentException(
              String.format("The parameter %s lost %s.", report.toString(), timeName)
      );
    }

    try {
      report.getLong(timeName);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }
    return true;
  }

  private static boolean checkLegalEpoch(
      int lastEpoch, int epoch, String deployId
  ) throws IllegalArgumentException {
    if (lastEpoch == epoch) {
      return true;
    } else {
      String msg = String.format(
          "Last epoch in server is %d. The epoch of report is %d and is illegal. %s-%d maybe left cluster.", lastEpoch, epoch, deployId, epoch
      );
      throw new IllegalArgumentException(msg);
    }
  }

  @Override
  public Future<Void> reportExecRunning(JsonObject running) {
    String timeName = "beatTime";
    try {
      checkExecStatus(running, ExecState.RUNNING);
      checkExecReport(running);
      checkTime(running, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String execId = running.getString("id");
    String deployId = running.getString("deployId");
    int epoch = running.getInteger("epoch");
    long beatTime = running.getLong(timeName);

    return execService.getStatus(execId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          ExecState lastState = ExecState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(ExecState.FAILURE) || lastState.equals(ExecState.FINISH)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", ExecState.RUNNING.toString())
              .put(timeName, beatTime);
          return execService.updateStatus(execId, update);
        });
  }

  @Override
  public Future<Void> reportExecFinish(JsonObject finish) {
    String timeName = "finishTime";
    try {
      checkExecStatus(finish, ExecState.FINISH);
      checkExecReport(finish);
      checkTime(finish, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String execId = finish.getString("id");
    String deployId = finish.getString("deployId");
    int epoch = finish.getInteger("epoch");
    long finishTime = finish.getLong(timeName);

    return execService.getStatus(execId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          ExecState lastState = ExecState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(ExecState.FAILURE) || lastState.equals(ExecState.FINISH)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", ExecState.FINISH.toString())
              .put(timeName, finishTime);
          return execService.updateStatus(execId, update);
        });
  }

  @Override
  public Future<Void> reportExecFailure(JsonObject failure) {
    String timeName = "terminateTime";
    try {
      checkExecStatus(failure, ExecState.FAILURE);
      checkExecReport(failure);
      checkTime(failure, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String execId = failure.getString("id");
    String deployId = failure.getString("deployId");
    int epoch = failure.getInteger("epoch");
    long terminateTime = failure.getLong(timeName);
    String failureMsg = failure.getString("msg");

    return execService.getStatus(execId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          ExecState lastState = ExecState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(ExecState.FAILURE) || lastState.equals(ExecState.FINISH)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", ExecState.FAILURE.toString())
              .put(timeName, terminateTime)
              .put("msg", failureMsg);
          return execService.updateStatus(execId, update);
        });
  }

  @Override
  public Future<Void> reportBackendUp(JsonObject up) {
    String timeName = "upTime";
    try {
      checkBackendStatus(up, BackendState.UP);
      checkBackendReport(up);
      checkTime(up, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String deployId = up.getString("deployId");
    int epoch = up.getInteger("epoch");
    long upTime = up.getLong(timeName);

    return projectService.getBackendStatusOfDeployId(deployId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = -1;
          try {
            lastEpoch = lastStatus.getInteger(ProjectDB.FIELD_BACKEND_STATUS_EPOCH);
          } catch (Exception e) {
            return Future.failedFuture(e);
          }

          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          BackendState lastState = BackendState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(BackendState.DOWN)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", BackendState.UP.toString())
              .put(timeName, upTime);

          return projectService.updateStatus(deployId, update)
              .compose(ret -> {return Future.succeededFuture();});
        }, error -> {
          LOGGER.error(error);
          return Future.failedFuture(error);
        });
  }

  @Override
  public Future<Void> reportBackendDown(JsonObject down) {
    String timeName = "downTime";
    try {
      checkBackendStatus(down, BackendState.DOWN);
      checkBackendReport(down);
      checkTime(down, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String deployId = down.getString("deployId");
    int epoch = down.getInteger("epoch");
    long downTime = down.getLong(timeName);

    return projectService.getBackendStatusOfDeployId(deployId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          BackendState lastState = BackendState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(BackendState.DOWN)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put("status", BackendState.DOWN.toString())
              .put(timeName, downTime);

          return projectService.updateStatus(deployId, update)
              .compose(ret -> {return Future.succeededFuture();});
        });
  }

  @Override
  public Future<Void> reportBackendFailure(JsonObject failure) {
    String timeName = "failureTime";
    try {
      checkBackendStatus(failure, BackendState.FAILURE);
      checkBackendReport(failure);
      checkTime(failure, timeName);
    } catch (IllegalArgumentException e) {
      return Future.failedFuture(e);
    }

    String deployId = failure.getString("deployId");
    int epoch = failure.getInteger("epoch");
    long failureTime = failure.getLong(timeName);
    String failureMsg = failure.getString("msg");

    return projectService.getBackendStatusOfDeployId(deployId)
        .compose((JsonObject lastStatus) -> {
          int lastEpoch = lastStatus.getInteger("epoch");
          try {
            checkLegalEpoch(lastEpoch, epoch, deployId);
          } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
          }

          BackendState lastState = BackendState.valueOf(lastStatus.getString("status"));
          if (lastState.equals(BackendState.DOWN)) {
            String msg = String.format("The status of exec is %s and terminated.", lastState.toString());
            return Future.failedFuture(msg);
          }

          JsonObject update = new JsonObject();
          update.put(ProjectDB.FIELD_BACKEND_STATUS_STATUS, BackendState.FAILURE.toString())
              .put(ProjectDB.FIELD_BACKEND_STATUS_FAILURE_MSG,
                  new JsonObject()
                      .put(ProjectDB.FIELD_BACKEND_STATUS_FAILURE_MSG_TIME, failureTime)
                      .put(ProjectDB.FIELD_BACKEND_STATUS_FAILURE_MSG_MSG, failureMsg)
              );

          return projectService.updateStatus(deployId, update)
              .compose(ret -> {return Future.succeededFuture();});
        });
  }
}
