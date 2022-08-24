package org.metal.backend;

import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.arrow.vector.types.pojo.Schema;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

enum BackendAPIStatus {
  UPPING,
  LIGHT,
  ANALYSING,
  EXECUTING,
  DOWNING
}

public class BackendAPI extends AbstractVerticle {

  private String id;
  private IBackend backend;
  private HttpServer server;

  private AtomicInteger status = new AtomicInteger(0);

  public BackendAPI(String id, IBackend backend) {
    this.id = id;
    this.backend = backend;
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

  private void execAdmit(RoutingContext ctx) {
    if (status.compareAndSet(BackendAPIStatus.LIGHT.ordinal(),
        BackendAPIStatus.EXECUTING.ordinal())) {
      ctx.next();
    } else {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", "The request of execution should wait a moment.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    }
  }

  private void specAdmit(RoutingContext ctx) {
    if (status.compareAndSet(BackendAPIStatus.LIGHT.ordinal(),
        BackendAPIStatus.ANALYSING.ordinal())) {
      ctx.next();
    } else {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", "The request of analysis should wait a moment.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    }
  }

  private void schemaAdmit(RoutingContext ctx) {
    if (status.get() == BackendAPIStatus.ANALYSING.ordinal()) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", "The request of schema should wait a moment.");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    } else {
      ctx.next();
    }
  }

  private void spec(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    JsonObject resp = new JsonObject();
    try {
      System.out.println(body.toString());
      Spec spec = new SpecFactoryOnJson().get(body.toString());
      Draft draft = DraftMaster.draft(spec);
      backend.service().analyse(draft);
      List<String> analysed = backend.service().analysed();
      List<String> unanalysed = backend.service().unAnalysed();
      resp = new JsonObject();
      resp.put("status", "OK")
          .put("data",
              new JsonObject()
                  .put("analysed", analysed)
                  .put("unanalysed", unanalysed)
          );

    } catch (IOException e) {
      resp.put("status", "FAIL").put("msg", "Fail to parse spec your post.");
    } catch (NullPointerException | NoSuchElementException | IllegalArgumentException e) {
      resp.put("status", "FAIL").put("msg", "Fail to parse spec your post.");
    } catch (MetalAnalysedException e) {
      resp.put("status", "FAIL").put("msg", "Fail to analyse metal.");
    } finally {
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
      if (!status.compareAndSet(BackendAPIStatus.ANALYSING.ordinal(),
          BackendAPIStatus.LIGHT.ordinal())) {
        System.out.println("STATUS ILLEGE.");
      }
    }
  }

  private void specRestApi(Router router) {
    router.route(HttpMethod.POST, "/spec")
        .produces("application/json")
        .handler(BodyHandler.create())
        .handler(this::specAdmit)
        .blockingHandler(this::spec, true);
  }

  private void heartRestApi(Router router) {
    router.route(HttpMethod.GET, "/heart")
        .produces("application/json")
        .handler((RoutingContext ctx) -> {
          ctx.response()
              .putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf("{\"status\": \"OK\"}".length()))
              .end("{\"status\": \"OK\"}");
        });
  }

  private void schemaRestApi(Router router) {
    router.route(HttpMethod.GET, "/schemas/:mid")
        .produces("application/json")
        .handler(this::schemaAdmit)
        .handler((RoutingContext ctx) -> {
          String mid = ctx.pathParam("mid");
          JsonObject resp = new JsonObject();
          try {
            Schema schema = backend.service().schema(mid);
            resp.put("status", "OK")
                .put("data", new JsonObject()
                    .put("id", mid)
                    .put("schema", new JsonObject(schema.toJson()))
                );
          } catch (MetalServiceException e) {
            resp.put("status", "FAIL")
                .put("msg", "Fail to found schema");
          } finally {
            String payload = resp.toString();
            ctx.response()
                .putHeader("content-type", ctx.getAcceptableContentType())
                .putHeader("content-length", String.valueOf(payload.length()))
                .end(payload);
          }
        });
  }

  private void execRestApi(Router router) {
    router.route(HttpMethod.POST, "/execution")
        .produces("application/json")
        .handler(this::execAdmit)
        .handler((RoutingContext ctx) -> {
          JsonObject resp = new JsonObject();
          boolean isExec = true;
          if (backend.service().analysed().isEmpty()) {
            isExec = false;
            resp.put("status", "FAIL")
                .put("msg", "Not any analysed metal in context.");

          }
          if (!backend.service().unAnalysed().isEmpty()) {
            isExec = false;
            resp.put("status", "FAIL")
                .put("msg", "Some unAnalysed metals exist in context.");
          }

          if (isExec) {
            ctx.next();
            resp.put("status", "OK");
            resp.put("msg", "Success to execute.");
          } else {
            if (!status.compareAndSet(BackendAPIStatus.EXECUTING.ordinal(),
                BackendAPIStatus.LIGHT.ordinal())) {
              System.out.println("STATUS ILLEGE.");
            }
          }

          String payload = resp.toString();
          ctx.response()
              .putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);

        })
        .blockingHandler((RoutingContext ctx) -> {
          try {
            backend.service().exec();
          } catch (MetalExecuteException e) {
            
          } finally {
            if (!status.compareAndSet(BackendAPIStatus.EXECUTING.ordinal(),
                BackendAPIStatus.LIGHT.ordinal())) {
              System.out.println("STATUS ILLEGE.");
            }
          }
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
    HttpServerOptions options = new HttpServerOptions();
    server = getVertx().createHttpServer(options);

    Router metalAPI = Router.router(getVertx());
    heartRestApi(metalAPI);
    specRestApi(metalAPI);
    execRestApi(metalAPI);
    schemaRestApi(metalAPI);

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

    server.listen(18000)
        .onFailure(t -> {
          System.out.println("Fail to create http server.");
          startPromise.fail(t);
        })
        .onSuccess(srv -> {
          System.out.println("Success to create http server.");
          status.set(BackendAPIStatus.LIGHT.ordinal());
          startPromise.complete();
        });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    status.set(BackendAPIStatus.DOWNING.ordinal());
    server.close().onFailure(t -> {
      System.out.println("Fail to close http server.");
      stopPromise.fail(t);
    }).onSuccess(srv -> {
      System.out.println("Success to close http server");
      stopPromise.complete();
    });
  }

  public static void main(String[] args) {
    IBackend backend = BackendManager.getBackendBuilder().get()
        .conf("master", "local[*]")
        .build();
    backend.start();

    VertxOptions vertxOptions = new VertxOptions();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    Vertx vertx = Vertx.vertx(vertxOptions);
    String id = "aaaaa-zzzzz";
    BackendAPI api = new BackendAPI(id, backend);

    deploymentOptions.setWorker(false).setInstances(1);
    vertx.deployVerticle(api, deploymentOptions)
        .onSuccess(msg -> {
          System.out.println("Success to deploy verticle.");
          System.out.println(msg);
        })
        .onFailure(t -> {
          System.out.println("Fail to deploy verticle.");
        });

  }
}
