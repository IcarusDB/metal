package org.metal.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.concurrent.atomic.AtomicInteger;
import org.metal.backend.api.BackendService;
import org.metal.backend.api.impl.BackendServiceImpl;
import org.metal.backend.rest.IBackendRestEndApi;
import org.metal.server.api.BackendReportError;
import org.metal.server.api.BackendReportService;
import org.metal.server.api.BackendState;

public class BackendGateway extends AbstractVerticle {

  private final static Logger LOGGER = LoggerFactory.getLogger(BackendGateway.class);
  private AtomicInteger state = new AtomicInteger(BackendState.CREATED.ordinal());
  private IBackend backend;
  private HttpServer httpServer;
  private IBackendRestEndApi api;
  private BackendService backendService;
  private MessageConsumer<JsonObject> consumer;
  private BackendReportService backendReportService;
  private String deployId;
  private int epoch;
  private int port;
  private long reportDelay = 5000l;
  private String reportAddress;

  public BackendGateway(IBackend backend) {
    this.backend = backend;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    deployId = config().getString("deployId");
    epoch = config().getInteger("epoch");
    reportAddress = config().getString("reportServiceAddress");

    WorkerExecutor workerExecutor = getVertx().createSharedWorkerExecutor("exec", 1);
    backendService = BackendServiceImpl.concurrency(
        getVertx(),
        backend,
        workerExecutor,
        deployId,
        epoch,
        reportAddress
    );
    ServiceBinder binder = new ServiceBinder(getVertx());

    String address = deployId + "-" + epoch;
    binder.setAddress(address);
    consumer = binder.register(BackendService.class, backendService);
    api = IBackendRestEndApi.create(backendService);

    backendReportService = BackendReportService.create(
        getVertx(),
        new JsonObject().put("address", reportAddress));

    state.set(BackendState.UP.ordinal());
    JsonObject up = new JsonObject();
    up.put("status", BackendState.UP.toString())
        .put("epoch", epoch)
        .put("deployId", deployId)
        .put("upTime", System.currentTimeMillis());
    backendReportService.reportBackendUp(up).onSuccess(ret -> {
      startPromise.complete();
    }).onFailure(error -> {
      error.printStackTrace();
      startPromise.fail(error);
    });

    getVertx().setPeriodic(reportDelay, (ID) -> {
      JsonObject upReport = new JsonObject();
      upReport.put("status", BackendState.UP.toString())
          .put("epoch", epoch)
          .put("deployId", deployId)
          .put("upTime", System.currentTimeMillis());
      backendReportService.reportBackendUp(upReport)
          .onSuccess(ret -> {
            String msg = String.format("Backend[%s-%d] report current is up.", deployId, epoch);
            LOGGER.info(msg);
          })
          .onFailure(error -> {
            LOGGER.error(error);
            if (error.getLocalizedMessage()
                .startsWith("[" + BackendReportError.EPOCH_ILLEGAL.toString() + "]") ||
                error.getLocalizedMessage()
                    .startsWith("[" + BackendReportError.MARKED_DOWN.toString() + "]") ||
                error.getLocalizedMessage()
                    .startsWith("[" + BackendReportError.MARKED_FAILURE.toString() + "]")
            ) {
              try {
                this.stop(Promise.promise());
              } catch (Exception e) {
                LOGGER.error(e);
              } finally {
                System.exit(0);
              }
            }
          });
    });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    state.set(BackendState.DOWN.ordinal());
    JsonObject down = new JsonObject();
    down.put("status", BackendState.DOWN.toString())
        .put("epoch", epoch)
        .put("deployId", deployId)
        .put("downTime", System.currentTimeMillis());
    backendReportService.reportBackendDown(down).compose(ret -> {
      return httpServer.close();
    }).compose(ret -> {
      return consumer.unregister();
    }).compose(ret -> {
      try {
        backend.stop();
        return Future.succeededFuture();
      } catch (Exception e) {
        return Future.failedFuture(e);
      }
    }).onSuccess(ret -> {
      stopPromise.complete();
    }).onFailure(error -> {
      stopPromise.fail(error);
    });
  }
}
