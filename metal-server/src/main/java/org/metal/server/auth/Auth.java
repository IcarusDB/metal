package org.metal.server.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.RoleBasedAuthorization;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.auth.mongo.MongoAuthorization;
import io.vertx.ext.auth.mongo.MongoAuthorizationOptions;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Auth extends AbstractVerticle {

  private final static String HASH_ALGO = "sha256";
  private final static Logger LOGGER = LoggerFactory.getLogger(Auth.class);
  private MongoClient mongo;
  private MongoAuthentication authenticationProvider;
  private MongoAuthorization authorizationProvider;
  private JWTAuth jwtAuth;
  private HttpServer server;

  private Auth(MongoClient client) {
    this.mongo = client;
    MongoAuthenticationOptions options = new MongoAuthenticationOptions();
    authenticationProvider = MongoAuthentication.create(mongo, options);

    authorizationProvider = MongoAuthorization.create("authorization", mongo,
        new MongoAuthorizationOptions());

    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions();
    jwtAuthOptions.addPubSecKey(
        new PubSecKeyOptions().setAlgorithm("HS256").setBuffer("123456")
    );
    jwtAuth = JWTAuth.create(getVertx(), jwtAuthOptions);

  }

  public static Future<Auth> create(MongoClient client) {
    Auth auth = new Auth(client);
    return auth.mongo.createIndexWithOptions("user",
            new JsonObject().put("username", 1),
            new IndexOptions().unique(true))
        .compose(ar -> {
          return Future.succeededFuture(auth);
        });
  }

  private static boolean checkRole(String role) {
    try {
      Roles.valueOf(role);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static boolean checkRoles(String[] roles) {
    for (int idx = 0; idx < roles.length; idx++) {
      if (!checkRole(roles[idx])) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkRoles(JsonArray roles) {
    for (int idx = 0; idx < roles.size(); idx++) {
      if (!checkRole(roles.getString(idx))) {
        return false;
      }
    }
    return true;
  }

  public void registerUser(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    String username = body.getString("username");
    String password = body.getString("password");
    JsonArray roles = body.getJsonArray("roles", new JsonArray().add(Roles.USER.toString()));

    if (!checkRoles(roles)) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", String.format("roles[%s] must be of %s.", roles, Roles.values()));
      String payload = resp.toString();
      ctx.response().putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
      return;
    }

    String hash = authenticationProvider.hash(HASH_ALGO, "", password);
    mongo.insert("user", new JsonObject()
            .put("username", username)
            .put("password", hash)
            .put("roles", roles))
        .onSuccess(userId -> {
          LOGGER.info("Success to register user:" + userId);
          JsonObject resp = new JsonObject();
          resp.put("status", "OK")
              .put("userId", userId);
          String payload = resp.toString();
          ctx.response().putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);
        })
        .onFailure(error -> {
          LOGGER.error("Fail to register user.", error);
          JsonObject resp = new JsonObject();
          resp.put("status", "FAIL")
              .put("msg", error.getLocalizedMessage());
          String payload = resp.toString();
          ctx.response().putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);
        });
  }

  public Future<User> attachRoles(User user) {
    return mongo.findOne("user",
            new JsonObject().put("username", user.get("username")),
            new JsonObject().put("roles", true))
        .compose(json -> {
          JsonArray roles = json.getJsonArray("roles");
          for (int idx = 0; idx < roles.size(); idx++) {
            RoleBasedAuthorization authorization = RoleBasedAuthorization.create(
                roles.getString(idx));
            user.authorizations().add("", authorization);
          }
          return Future.succeededFuture(user);
        });
  }

  public void jwt(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    String username = body.getString("username");
    String password = body.getString("password");
    JsonObject authInfo = new JsonObject()
        .put("username", username)
        .put("password", password);

    LOGGER.info(authInfo);

    authenticationProvider.authenticate(authInfo)
        .compose(this::attachRoles)
        .compose(user -> {
          ctx.setUser(user);
          return Future.succeededFuture(user);
        })
        .onSuccess(user -> {
          LOGGER.info(user);
          String jwt = jwtAuth.generateToken(
              new JsonObject().put("username", user.get("username")));
          JsonObject resp = new JsonObject()
              .put("status", "OK")
              .put("jwt", jwt);
          String payload = resp.toString();
          ctx.response()
              .putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);
        })
        .onFailure(error -> {
          LOGGER.error("Fail to authenticate.");
          LOGGER.error(error);
          JsonObject resp = new JsonObject();
          resp.put("status", "FAIL")
              .put("msg", "Fail to authenticate.");
          String payload = resp.toString();
          ctx.response()
              .putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);
        });
  }

  public void authenticationOnJwt(RoutingContext ctx) {
    String authorization = ctx.request().getHeader("Authorization");
    String[] bearer = authorization.split("Bearer");
    if (bearer == null || bearer.length < 2) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", "Fail to authentication");
      String payload = resp.toString();
      ctx.response()
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
      return;
    }
    String jwt = bearer[1].strip();
    jwtAuth.authenticate(new JsonObject().put("token", jwt))
        .compose(this::attachRoles)
        .compose(user -> {
          ctx.setUser(user);
          return Future.succeededFuture(user);
        })
        .onSuccess(user -> {
          LOGGER.info(user);
          ctx.next();
        })
        .onFailure(error -> {
          LOGGER.error(error);
          JsonObject resp = new JsonObject();
          resp.put("status", "FAIL")
              .put("msg", error.getLocalizedMessage());
          String payload = resp.toString();
          ctx.response()
              .putHeader("content-type", ctx.getAcceptableContentType())
              .putHeader("content-length", String.valueOf(payload.length()))
              .end(payload);
        });
  }

  private void something(RoutingContext ctx) {
    ctx.response().end("OK");
  }
}
