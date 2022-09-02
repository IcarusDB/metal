package org.metal.server;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.metal.server.auth.AttachRoles;
import org.metal.server.auth.Auth;
import org.metal.server.auth.Roles;
import org.metal.server.repo.Repo;

public class Server extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

  private IServerProps props;
  private HttpServer httpServer;
  private MongoClient mongo;
  private Auth auth;
  private Repo repo;

  public Server(IServerProps props) {
    this.props = props;
  }

  private void prepareDB() {

  }

  private static void response(RoutingContext ctx, JsonObject resp) {
    String payload = resp.toString();
    ctx.response()
        .putHeader("content-type", ctx.getAcceptableContentType())
        .putHeader("content-length", String.valueOf(payload.length()))
        .end(payload);
  }

  private void createUser(RoutingContext ctx) {
    String userName = ctx.pathParam("name");
    JsonObject body = ctx.body().asJsonObject();
    Optional<User> user = User.of(body);

    JsonObject resp = new JsonObject();
    if (user.isEmpty()) {
      resp.put("status", "FAIL")
          .put("msg", String.format("Fail to create one user from %s.", body.toString()));
      response(ctx, resp);
      return;
    }
    if (!user.get().name().equals(userName)) {
      resp.put("status", "FAIL")
          .put("msg", "Fail to create one user, because user name in body is not equal name in path.");
      response(ctx, resp);
      return;
    }

    JsonObject query = new JsonObject().put("name", userName);
    mongo.insert("users", User.json(user.get()))
        .onSuccess(id -> {
          resp.put("status", "OK")
              .put("id", id);
          response(ctx, resp);
        })
        .onFailure(t -> {
          resp.put("status", "FAIL")
              .put("msg", t.getLocalizedMessage());
          response(ctx, resp);
        });
  }

  private void findUsers(RoutingContext ctx) {
    mongo.find("users", new JsonObject())
        .onSuccess(result -> {
          JsonObject resp = new JsonObject()
              .put("status", "OK")
              .put("data", result);
          response(ctx, resp);
        })
        .onFailure(t -> {
          JsonObject resp = new JsonObject()
              .put("status", "FAIL")
              .put("msg", "Fail to find users.");
          response(ctx, resp);
        });
  }

  private Future<Router> createRestAPI(Router router) {
    router.post("/api/v1/users")
        .produces("application/json")
        .handler(BodyHandler.create())
        .handler(this.auth::registerUser);

    router.post("/api/v1/tokens")
        .produces("application/json")
        .handler(BodyHandler.create())
        .handler(BasicAuthHandler.create(
            this.auth.getAuthenticationProvider()
        ))
        .handler(BodyJsonValid::valid)
        .handler(this.auth::jwt);

    router.route("/api/v1/something")
        .produces("application/json")
        .handler(JWTAuthHandler.create(this.auth.getJwtAuth()))
        .handler(AttachRoles.create(mongo)::attach)
//        .handler(this.auth::authenticationOnJwt)
        .handler(this::something);


    repo.createRepoProxy(router, getVertx());
    router.post("/api/v1/repo/package")
        .handler(repo::deploy);
    return Future.succeededFuture(router);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    mongo = MongoClient.createShared(getVertx(), new JsonObject()
        .put("connection_string", props.mongoConnection())
    );

    httpServer = getVertx().createHttpServer();
    repo = new Repo();
    Auth.create(mongo)
        .compose((Auth auth) -> {
          this.auth = auth;
          return Future.succeededFuture(auth);
        })
        .compose((Auth auth) -> {
          Router router = Router.router(getVertx());
          return Future.succeededFuture(router);
        })
        .compose(this::createRestAPI)
        .compose((Router router) -> {
          httpServer.requestHandler(router);
          return httpServer.listen(props.port());
        })
        .onSuccess(srv -> {
          LOGGER.info(String.format("Success to start Server[%s] on port[%d].", Server.class, srv.actualPort()));
          startPromise.complete();
        })
        .onFailure(error -> {
          LOGGER.error(error);
          startPromise.fail(error);
        });;
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    httpServer.close()
        .compose(ret -> {
          return mongo.close();
        }, error -> {
          LOGGER.error(String.format("Fail to stop Server[%s] on port[%d].", Server.class, props.port()), error);
          return mongo.close();
        })
        .onSuccess(ret ->{
          LOGGER.info(String.format("Success to close mongoDB connection."));
        })
        .onFailure(error->{
          LOGGER.info(String.format("Fail to close mongoDB connection."), error);
        });
  }

  private void something(RoutingContext ctx) {
    String author = ctx.user().authorizations().getProviderIds().toString();
    boolean isRole = RoleBasedAuthorization.create(Roles.USER.toString()).match(ctx.user());
    ctx.response().end(ctx.user().get("username").toString() + ":" + isRole + ":" + author);
  }
}
