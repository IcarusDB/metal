package org.metal.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.concurrent.atomic.AtomicInteger;
import org.metal.backend.api.BackendService;
import org.metal.server.api.BackendReportService;
import org.metal.server.api.BackendState;
import org.metal.backend.api.impl.BackendServiceImpl;
import org.metal.backend.rest.IBackendRestEndApi;

public class BackendGateway extends AbstractVerticle {

  private AtomicInteger state = new AtomicInteger(BackendState.UN_DEPLOY.ordinal());
  private IBackend backend;
  private HttpServer httpServer;
  private IBackendRestEndApi api;
  private BackendService backendService;
  private MessageConsumer<JsonObject> consumer;
  private BackendReportService backendReportService;
  private String deployId;
  private int epoch;
  private int port;
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

//    port = config().getInteger("restApiPort");
//    httpServer = getVertx().createHttpServer();
//    Router router = Router.router(getVertx());
//    router.post("/api/v1/spec")
//        .produces("application/json")
//        .handler(BodyHandler.create())
//        .handler(api::analyseAPI);
//
//    router.post("/api/v1/exec")
//        .produces("application/json")
//        .handler(api::execAPI);
//
//    router.get("/api/v1/schemas/:mid")
//        .produces("application/json")
//        .handler(api::schemaAPI);
//    httpServer.requestHandler(router);
//    httpServer.listen(port)
//        .compose(ret -> {
//          try {
//            /**
//             * Cancel backend start in here.
//             */
////            backend.start();
//            return Future.succeededFuture();
//          } catch (Exception e) {
//            return Future.failedFuture(e);
//          }
//        }).compose(ret -> {
//          state.set(BackendState.UP.ordinal());
//          JsonObject up = new JsonObject();
//          up.put("status", BackendState.UP.toString())
//              .put("epoch", epoch)
//              .put("deployId", deployId)
//              .put("upTime", System.currentTimeMillis());
//          return backendReportService.reportBackendUp(up);
//        }, error -> {
//          state.set(BackendState.FAILURE.ordinal());
//          JsonObject fail = new JsonObject();
//          fail.put("status", BackendState.FAILURE.toString())
//              .put("epoch", epoch)
//              .put("deployId", deployId)
//              .put("failureTime", System.currentTimeMillis())
//              .put("msg", error.getLocalizedMessage());
//          return backendReportService.reportBackendFailure(fail);
//        }).onSuccess(ret -> {
//          startPromise.complete();
//        }).onFailure(error -> {
//          startPromise.fail(error);
//        });
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
