package org.metal.server.repo;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;
import java.nio.charset.StandardCharsets;
import org.bson.internal.Base64;

public class Repo {
  private final static Logger LOGGER = LoggerFactory.getLogger(Repo.class);
  private final static String MVN_REPO_API_HOST = "124.223.66.8";
  private final static int MVN_REPO_API_PORT = 8081;
  private final static String MVN_REPO_API_URL = "/service/rest/v1";
  private final static String credential = new UsernamePasswordCredentials("admin", "123456").toHttpAuthorization();
  private final static String repository = "maven-releases";

  public void createRepoProxy(Router router, Vertx vertx) {
    HttpClient client = vertx.createHttpClient(new HttpClientOptions().setShared(true));
    HttpProxy proxy = HttpProxy.reverseProxy(client);
    proxy.origin(MVN_REPO_API_PORT, MVN_REPO_API_HOST);
    ProxyHandler proxyHandler = ProxyHandler.create(proxy);
    router.post(MVN_REPO_API_URL + "/components")
        .handler(ctx -> {
          ctx.request().headers().add("Authorization", credential);
          ctx.next();
        })
        .handler(proxyHandler);
  }

  public void deploy(RoutingContext ctx) {
    ctx.reroute(HttpMethod.POST, MVN_REPO_API_URL + "/components?repository=" + repository);
  }

  public void submitPackageManifest(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

  }
}
