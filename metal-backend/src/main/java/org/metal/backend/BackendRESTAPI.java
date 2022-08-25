package org.metal.backend;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.metal.backend.IBackendAPI.ConcurrentBackendAPI;

enum BackendAPIStatus {
  UPPING,
  RUNNING,
  DOWNING
}

public class BackendRESTAPI extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(BackendRESTAPI.class);

  private String id;
  private IBackendRESTAPIProps props;
  private IBackend backend;
  private ConcurrentBackendAPI API;
  private HttpServer server;

  private AtomicInteger status = new AtomicInteger(0);

  public BackendRESTAPI(String id, IBackend backend) {
    this.id = id;
    this.backend = backend;
    this.API = IBackendAPI.of(new BackendAPIImpl(backend));
    this.props = ImmutableIBackendRESTAPIProps.builder()
        .id(this.id)
        .build();
  }

  public BackendRESTAPI(IBackendRESTAPIProps props, IBackend backend) {
    this.id = props.id();
    this.props = props;
    this.backend = backend;
    this.API = IBackendAPI.of(new BackendAPIImpl(backend));
  }

  private void restAdmit(RoutingContext ctx) {
    if (status.get() == BackendAPIStatus.UPPING.ordinal()) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", "Backend is upping. The request should wait a moment.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    } else if (status.get() == BackendAPIStatus.DOWNING.ordinal()) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", "Backend is downing.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    } else {
      ctx.next();
    }
  }


  private void onNonAcquire(RoutingContext ctx) {
    JsonObject resp = new JsonObject();
    resp.put("status", "FAIL")
        .put("acquired", "FALSE");

    String payload = resp.toString();
    ctx.response()
        .putHeader("content-type", ctx.getAcceptableContentType())
        .putHeader("content-length", String.valueOf(payload.length()))
        .end(payload);
  }

  private void specRESTAPI(Router router) {
    router.route(HttpMethod.POST, "/spec")
        .produces("application/json")
        .handler(BodyHandler.create())
        .blockingHandler((RoutingContext ctx) -> {
          this.API.tryAnalyseAPI(ctx, this::onNonAcquire);
        }, true);
  }

  private void heartRESTAPI(Router router) {
    router.route(HttpMethod.GET, "/heart")
        .produces("application/json")
        .handler(this.API::heartAPI);
  }

  private void schemaRESTAPI(Router router) {
    router.route(HttpMethod.GET, "/schemas/:mid")
        .produces("application/json")
        .handler((RoutingContext ctx) -> {
          this.API.trySchemaAPI(ctx, this::onNonAcquire);
        });
  }

  private void executionRESTAPI(Router router) {
    router.route(HttpMethod.POST, "/execution")
        .produces("application/json")
        .blockingHandler((RoutingContext ctx) -> {
          this.API.tryExecAPI(ctx, this::onNonAcquire);
        }, true);
  }

  private void identify(RoutingContext ctx) {
    String pathID = ctx.pathParam("id");
    if (!pathID.equals(id)) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", pathID + " is NOT FOUND.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .setStatusCode(404)
          .end(payload);
    } else {
      ctx.next();
    }
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    status.set(BackendAPIStatus.UPPING.ordinal());
    LOGGER.info(String.format("Backend[%s] is Starting.", backend.getClass()));
    try {
      backend.start();
    } catch (Exception e) {
      startPromise.fail(e);
    }

    HttpServerOptions options = new HttpServerOptions();
    server = getVertx().createHttpServer(options);

    Router metalAPI = Router.router(getVertx());
    heartRESTAPI(metalAPI);
    specRESTAPI(metalAPI);
    executionRESTAPI(metalAPI);
    schemaRESTAPI(metalAPI);

    Router identifyAPI = Router.router(getVertx());
    identifyAPI.route("/:id*")
        .produces("application/json")
        .handler(this::identify)
        .subRouter(metalAPI);

    Router restAPIv1 = Router.router(getVertx());
    restAPIv1.route("/v1*")
        .handler(this::restAdmit)
        .subRouter(identifyAPI);

    server.requestHandler(restAPIv1);
    Future<HttpServer> serverFuture;
    if (props.port().isPresent()) {
      serverFuture = server.listen(props.port().get());
    } else {
      serverFuture = server.listen();
    }

    serverFuture.onFailure(t -> {
          LOGGER.error(String.format("Fail to create http server on port[%d].", server.actualPort()));
          startPromise.fail(t);
        })
        .onSuccess(srv -> {
          LOGGER.info(String.format("Success to create http server on port[%d].", server.actualPort()));
          status.set(BackendAPIStatus.RUNNING.ordinal());
          startPromise.complete();
        });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    status.set(BackendAPIStatus.DOWNING.ordinal());
    server.close().onFailure(t -> {
      LOGGER.error("Fail to close http server.");
      stopPromise.fail(t);
    }).onSuccess(srv -> {
      LOGGER.info("Success to close http server");
      stopPromise.complete();
    });
    LOGGER.info(String.format("Backend[%s] is stopping.", backend.getClass()));
    try {
      backend.stop();
    } catch (Exception e) {
      stopPromise.fail(e);
    }
  }

  public static void main(String[] args) {
    IBackend backend = BackendManager.getBackendBuilder().get()
        .conf("master", "local[*]")
        .build();

    VertxOptions vertxOptions = new VertxOptions();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);
    String id = "aaaaa-zzzzz";
    BackendRESTAPI api = new BackendRESTAPI(id, backend);

    deploymentOptions.setWorker(false).setInstances(1);
    vertx.deployVerticle(api, deploymentOptions)
        .onSuccess(msg -> {
          LOGGER.info("Success to deploy verticle:" + msg + ".");
        })
        .onFailure(t -> {
          LOGGER.error("Fail to deploy verticle.");
        });

  }
}
