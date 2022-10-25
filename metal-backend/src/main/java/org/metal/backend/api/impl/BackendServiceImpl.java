package org.metal.backend.api.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.arrow.vector.types.pojo.Schema;
import org.metal.backend.IBackend;
import org.metal.backend.api.BackendService;
import org.metal.server.api.BackendReportService;
import org.metal.server.api.BackendState;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalyseAcquireException;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalDraftException;
import org.metal.exception.MetalExecAcquireException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;
import org.metal.exception.MetalSpecParseException;
import org.metal.exception.MetalStatusAcquireException;
import org.metal.server.api.ExecState;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

public class BackendServiceImpl implements BackendService {
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendServiceImpl.class);

  private String deployId;
  private int epoch;
  private String reportAddress;
  private Vertx vertx;
  private IBackend backend;
  private WorkerExecutor workerExecutor;
  private BackendReportService reportor;

  public BackendServiceImpl(
      Vertx vertx,
      IBackend backend,
      WorkerExecutor workerExecutor,
      String deployId,
      int epoch,
      String reportAddress
  ) {
    this.deployId = deployId;
    this.epoch = epoch;
    this.reportAddress = reportAddress;
    this.vertx = vertx;
    this.backend = backend;
    this.workerExecutor = workerExecutor;
    this.reportor = BackendReportService.create(vertx, new JsonObject().put("address", reportAddress));
  }

  @Override
  public Future<JsonObject> analyse(JsonObject spec) {
    try {
      Spec specObj = new SpecFactoryOnJson().get(spec.toString());
      Draft draft = DraftMaster.draft(specObj);
      return workerExecutor.executeBlocking(promise -> {
        try {
          backend.service().analyse(draft);
          List<String> analysed = backend.service().analysed();
          List<String> unAnalysed = backend.service().unAnalysed();
          JsonObject resp = new JsonObject();
          resp.put("analysed", analysed)
              .put("unAnalysed", unAnalysed);
          promise.complete(resp);
        } catch (MetalAnalysedException e) {
          LOGGER.error(e);
          promise.fail(e);
        }
      });
    } catch (MetalSpecParseException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    } catch (MetalDraftException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    } catch (MetalAnalysedException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> schema(String metalId) {
    try {
      Schema schema = backend.service().schema(metalId);
      JsonObject resp = new JsonObject();
      resp.put("id", metalId)
          .put("schema", new JsonObject(schema.toJson()));
      return Future.succeededFuture(resp);
    } catch (MetalServiceException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    } catch (Exception e) {
      e.printStackTrace();
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> heart() {
    JsonObject resp = new JsonObject();
    resp.put("time", LocalDateTime.now().toString());
    return Future.succeededFuture(resp);
  }

  @Override
  public Future<JsonObject> status() {
    JsonObject resp = new JsonObject();
    resp.put("deployId", deployId);
    resp.put("epoch", epoch);
    resp.put("status", BackendState.UP.toString());
    resp.put("beatTime", System.currentTimeMillis());
    return Future.succeededFuture(resp);
  }

  @Override
  public Future<Void> exec(JsonObject exec) {
    String execId = exec.getString("id");

    if (backend.service().analysed().isEmpty()) {
      return Future.failedFuture("Not any analysed metal in context.");
    }

    if (!backend.service().unAnalysed().isEmpty()) {
      return Future.failedFuture("Some unAnalysed metals exist in context.");
    }

    JsonObject submit = new JsonObject();
    submit.put("id", execId)
        .put("deployId", deployId)
        .put("epoch", epoch)
        .put("status", ExecState.SUBMIT.toString())
        .put("submitTime", System.currentTimeMillis());

    reportor.reportExecSubmit(submit)
        .onFailure(error -> {
          LOGGER.error("Fail to report create exec " + execId, error);
        });

    return workerExecutor.executeBlocking(
        (promise) -> {
          try {
            JsonObject running = new JsonObject();
            running.put("id", execId)
                .put("deployId", deployId)
                .put("epoch", epoch)
                .put("status", ExecState.RUNNING.toString())
                .put("beatTime", System.currentTimeMillis());
            reportor.reportExecRunning(running)
                .onFailure(error -> {
                  LOGGER.error("Fail to report running exec " + execId, error);
                });

            backend.service().exec();
            JsonObject finish = new JsonObject();
            finish.put("id", execId)
                .put("deployId", deployId)
                .put("epoch", epoch)
                .put("status", ExecState.FINISH.toString())
                .put("finishTime", System.currentTimeMillis());
            reportor.reportExecFinish(finish)
                    .onFailure(error -> {
                      LOGGER.error("Fail to reprot finish exec " + execId, error);
                    });
            promise.complete();
          } catch (MetalExecuteException e) {
            JsonObject failure = new JsonObject();
            failure.put("id", execId)
                .put("deployId", deployId)
                .put("epoch", epoch)
                .put("status", ExecState.FAILURE)
                .put("terminateTime", System.currentTimeMillis())
                .put("msg", e.getLocalizedMessage());

            reportor.reportExecFailure(failure)
                .onFailure(error -> {
                  LOGGER.error("Fail to report failure of exec " + execId, error);
                });
            promise.fail(e);
          }
        }, true
    );
  }

  @Override
  public Future<Void> killExec(JsonObject exec) {
    return null;
  }

  @Override
  public Future<Void> stop() {
    return null;
  }

  @Override
  public Future<Void> gracefulStop() {
    return null;
  }

  public static BackendService concurrency(
      Vertx vertx,
      IBackend backend,
      WorkerExecutor workerExecutor,
      String deployId,
      int epoch,
      String reportAddress
  ) {
    return new ConcurrencyService(
        new BackendServiceImpl(vertx, backend, workerExecutor, deployId, epoch, reportAddress)
    );
  }

  private static class ConcurrencyService implements BackendService {
    private BackendService innerService;
    private ReadLock analyseReadLock;
    private WriteLock analyseWriteLock;
    private ReentrantLock execLock;

    private ConcurrencyService(BackendService backendService) throws IllegalArgumentException{
      if (backendService instanceof  ConcurrencyService) {
        throw new IllegalArgumentException(
            String.format("Fail to construct object, because the backendService is one instance of %s", ConcurrencyService.class)
        );
      }
      this.innerService = backendService;
      ReentrantReadWriteLock analyseLock = new ReentrantReadWriteLock();
      this.analyseReadLock = analyseLock.readLock();
      this.analyseWriteLock = analyseLock.writeLock();
      this.execLock = new ReentrantLock();
    }

    @Override
    public Future<JsonObject> analyse(JsonObject spec) {
      if (analyseWriteLock.tryLock()) {
        return innerService.analyse(spec).compose(
            ret -> {
              analyseWriteLock.unlock();
              return Future.succeededFuture(ret);
            },
            error -> {
              analyseWriteLock.unlock();
              return Future.failedFuture(error);
            }
        );
      }
      return Future.failedFuture(new MetalAnalyseAcquireException("Analyse has been acquired by other request, wait a moment."));
    }

    @Override
    public Future<JsonObject> schema(String metalId) {
      if (analyseReadLock.tryLock()) {
        return innerService.schema(metalId).compose(
            ret -> {
              analyseReadLock.unlock();
              return Future.succeededFuture(ret);
            },
            error -> {
              analyseReadLock.unlock();
              return Future.failedFuture(error);
            }
        );
      }
      return Future.failedFuture(new MetalAnalyseAcquireException("Now analyse has been acquired by other request, wait a moment."));
    }

    @Override
    public Future<JsonObject> heart() {
      return innerService.heart();
    }

    @Override
    public Future<JsonObject> status() {
      if (analyseReadLock.tryLock()) {
        return innerService.status().compose(
            ret -> {
              analyseReadLock.unlock();
              return Future.succeededFuture(ret);
            },
            error -> {
              analyseReadLock.unlock();
              return Future.failedFuture(error);
            }
        );
      }
      return Future.failedFuture(new MetalStatusAcquireException("Now analyse has been acquired by other request, wait a moment."));

    }

    @Override
    public Future<Void> exec(JsonObject exec) {
      if (analyseReadLock.tryLock()) {
        if (execLock.tryLock()) {
          return innerService.exec(exec).compose(
              ret -> {
                execLock.unlock();
                analyseReadLock.unlock();
                return Future.succeededFuture(ret);
              },
              error -> {
                execLock.unlock();
                analyseReadLock.unlock();
                return Future.failedFuture(error);
              }
          );
        }
        analyseReadLock.unlock();
        return Future.failedFuture(new MetalExecAcquireException("Now executor has been acquired by other request, wait a moment."));
      }
      return Future.failedFuture(new MetalExecAcquireException("Now analyse has been acquired by other request, wait a moment."));
    }

    @Override
    public Future<Void> killExec(JsonObject exec) {
      return null;
    }

    @Override
    public Future<Void> stop() {
      return null;
    }

    @Override
    public Future<Void> gracefulStop() {
      return null;
    }
  }
}
