package org.metal.backend.api.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
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
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

public class BackendServiceImpl implements BackendService {
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendServiceImpl.class);
  private IBackend backend;
  private WorkerExecutor workerExecutor;

  private BackendServiceImpl(IBackend backend, WorkerExecutor workerExecutor) {
    this.backend = backend;
    this.workerExecutor = workerExecutor;
  }

  @Override
  public Future<JsonObject> analyse(JsonObject spec) {
    try {
      Spec specObj = new SpecFactoryOnJson().get(spec.toString());
      Draft draft = DraftMaster.draft(specObj);
      backend.service().analyse(draft);
      List<String> analysed = backend.service().analysed();
      List<String> unAnalysed = backend.service().unAnalysed();
      JsonObject resp = new JsonObject();
      resp.put("analysed", analysed)
          .put("unAnalysed", unAnalysed);
      return Future.succeededFuture(resp);
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
  public Future<JsonObject> schemaAPI(String metalId) {
    try {
      Schema schema = backend.service().schema(metalId);
      JsonObject resp = new JsonObject();
      resp.put("id", metalId)
          .put("schema", new JsonObject(schema.toJson()));
      return Future.succeededFuture(resp);
    } catch (MetalServiceException e) {
      LOGGER.error(e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<JsonObject> heartAPI() {
    JsonObject resp = new JsonObject();
    resp.put("time", LocalDateTime.now());
    return Future.succeededFuture(resp);
  }

  @Override
  public Future<JsonObject> statusAPI() {
    return Future.succeededFuture();
  }

  @Override
  public Future<JsonObject> execAPI(JsonObject exec) {
    String execId = exec.getString("id");
    if (backend.service().analysed().isEmpty()) {
      return Future.failedFuture("Not any analysed metal in context.");
    }

    if (!backend.service().unAnalysed().isEmpty()) {
      return Future.failedFuture("Some unAnalysed metals exist in context.");
    }

    return workerExecutor.executeBlocking(
        (promise) -> {
          try {
            backend.service().exec();
            JsonObject resp = new JsonObject();
            resp.put("id", execId)
                .put("status", "FINISH")
                .put("finishTime", System.currentTimeMillis());
            promise.complete(resp);
          } catch (MetalExecuteException e) {
            promise.fail(e);
          }
        }, true
    );
  }

  public static BackendService concurrency(IBackend backend, WorkerExecutor workerExecutor) {
    return new ConcurrencyService(
        new BackendServiceImpl(backend, workerExecutor)
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
    public Future<JsonObject> schemaAPI(String metalId) {
      if (analyseReadLock.tryLock()) {
        return innerService.schemaAPI(metalId).compose(
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
    public Future<JsonObject> heartAPI() {
      return innerService.heartAPI();
    }

    @Override
    public Future<JsonObject> statusAPI() {
      if (analyseReadLock.tryLock()) {
        return innerService.statusAPI().compose(
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
    public Future<JsonObject> execAPI(JsonObject exec) {
      if (analyseReadLock.tryLock()) {
        if (execLock.tryLock()) {
          return innerService.execAPI(exec).compose(
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
  }
}
