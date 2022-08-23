package org.metal.backend;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.impl.HttpResponseHead;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.exception.MetalAnalysedException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.nio.Buffer;
import java.util.List;
import java.util.NoSuchElementException;

public class BackendAPI extends AbstractVerticle {

  private IBackend backend;
  private HttpServer server;
  private Router router;

  public BackendAPI(IBackend backend) {
    this.backend = backend;
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
      resp.put("analysed", analysed)
          .put("unanalysed", unanalysed)
          .put("status", "success");

    } catch (IOException e) {
      e.printStackTrace();
      resp.put("status", "error").put("msg", "Fail to parse spec your post.");
    } catch (NullPointerException | NoSuchElementException | IllegalArgumentException e) {
      e.printStackTrace();
      resp.put("status", "error").put("msg", "Fail to parse spec your post.");
    } catch (MetalAnalysedException e) {
      e.printStackTrace();
      resp.put("status", "error").put("msg", "Fail to analyse metal.");
    } finally {
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
    }
  }

  private void specRestApi(Router router) {
    router.route(HttpMethod.POST, "/spec")
        .produces("application/json")
        .handler(BodyHandler.create())
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

  private void execRestApi(Router router) {
      router.route(HttpMethod.POST, "/execution")
          .produces("application/json")
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
                  resp.put("status", "Success");
                  resp.put("msg", "Success to execute.");
              }

              String payload = resp.toString();
              ctx.response()
                  .putHeader("content-type", ctx.getAcceptableContentType())
                  .putHeader("content-length", String.valueOf(payload.length()))
                  .end(payload);

          })
          .blockingHandler((RoutingContext ctx) -> {
              System.out.println("Call Exec.");
              backend.service().exec();
          }, true);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServerOptions options = new HttpServerOptions();
    server = getVertx().createHttpServer(options);
    router = Router.router(getVertx());
    heartRestApi(router);
    specRestApi(router);
    execRestApi(router);

    server.requestHandler(router);
    server.listen(18000)
        .onFailure(t -> {
          System.out.println("Fail to create http server.");
          startPromise.fail(t);
        })
        .onSuccess(srv -> {
          System.out.println("Success to create http server.");
          startPromise.complete();
        });
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
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
    BackendAPI api = new BackendAPI(backend);

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
