package org.metal.server.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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
import io.vertx.ext.web.RoutingContext;
import org.metal.server.user.UserDB;
import org.metal.server.util.OnFailure;
import org.metal.server.util.RestServiceEnd;
import org.metal.server.util.SendJson;

public class Auth extends AbstractVerticle {

  public final static String HASH_ALGO = "sha256";
  private final static Logger LOGGER = LoggerFactory.getLogger(Auth.class);
  private MongoClient mongo;
  private MongoAuthentication authenticationProvider;
  private MongoAuthorization authorizationProvider;
  private JWTAuth jwtAuth;
  private HttpServer server;
  private AttachRoles attachRoles;

  public MongoAuthentication getAuthenticationProvider() {
    return authenticationProvider;
  }

  public MongoAuthorization getAuthorizationProvider() {
    return authorizationProvider;
  }

  public JWTAuth getJwtAuth() {
    return jwtAuth;
  }


  private Auth(MongoClient client) {
    this.mongo = client;
    MongoAuthenticationOptions options = new MongoAuthenticationOptions();
    authenticationProvider = MongoAuthentication.create(mongo, options);
    authorizationProvider = MongoAuthorization.create("authorization", mongo,
        new MongoAuthorizationOptions());

    attachRoles = AttachRoles.create(mongo);

    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions();
    jwtAuthOptions.addPubSecKey(
        new PubSecKeyOptions().setAlgorithm("HS256").setBuffer("123456")
    );
    jwtAuth = JWTAuth.create(getVertx(), jwtAuthOptions);
    jwtAuth = JWTAuthWithAttachRoles.create(jwtAuth, attachRoles);
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

  public void updateUserName(RoutingContext ctx) {
    User user = ctx.user();
    String userId = user.get("_id");
    JsonObject body = ctx.body().asJsonObject();
    String newName = body.getString("newName");

    if (
        OnFailure.doTry(ctx, () -> {
          return newName == null || newName.isBlank();
        }, "Fail to found legal newName in request.", 400)
    ) {
      return;
    }

    JsonObject matcher = new JsonObject()
        .put(UserDB.FIELD_ID, userId);
    JsonObject update = new JsonObject()
        .put("$set", new JsonObject().put(UserDB.FIELD_USER_NAME, newName));
    Future<JsonObject> result = UserDB.update(mongo, matcher, update);
    RestServiceEnd.end(ctx, result, LOGGER);
  }

  public void updateUserPassword(RoutingContext ctx) {
    User user = ctx.user();
    String userId = user.get("_id");
    JsonObject body = ctx.body().asJsonObject();
    String oldPassword = body.getString("oldPassword");
    String newPassword = body.getString("newPassword");
    if (
        OnFailure.doTry(ctx, () -> {
          return oldPassword == null || oldPassword.isBlank();
        }, "Fail to found legal oldPassword in request.", 400)
    ) {
      return;
    }

    if (
        OnFailure.doTry(ctx, () -> {
          return newPassword == null || newPassword.isBlank();
        }, "Fail to found legal newPassword in request.", 400)
    ) {
      return;
    }

    String oldHash = authenticationProvider.hash(HASH_ALGO, "", oldPassword);
    String newHash = authenticationProvider.hash(HASH_ALGO, "", newPassword);
    JsonObject matcher = new JsonObject()
        .put(UserDB.FIELD_ID, userId)
        .put(UserDB.FIELD_PASSWORD, oldHash);
    JsonObject update = new JsonObject()
        .put("$set", new JsonObject().put(UserDB.FIELD_PASSWORD, newHash));
    Future<JsonObject> result = UserDB.update(mongo, matcher, update);
    RestServiceEnd.end(ctx, result, LOGGER);
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
      SendJson.send(ctx, resp, 415);
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
          SendJson.send(ctx, resp, 201);
        })
        .onFailure(error -> {
          LOGGER.error("Fail to register user.", error);
          JsonObject resp = new JsonObject();
          resp.put("status", "FAIL")
              .put("msg", error.getLocalizedMessage());
          SendJson.send(ctx, resp, 409);
        });
  }

  public void createJWT(RoutingContext ctx) {
    LOGGER.info("Online User:" + ctx.user().get("username"));
    User user = ctx.user();

    if (user == null) {
      LOGGER.error("Fail to authenticate because no user has been authenticated.");
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", "Fail to authenticate.");
      SendJson.send(ctx, resp, 401);
      return;
    }

    String jwt = jwtAuth.generateToken(
        new JsonObject()
            .put("username", user.get("username"))
            .put("_id", user.get("_id"))
    );

    JsonObject resp = new JsonObject()
        .put("status", "OK")
        .put("jwt", jwt);
    SendJson.send(ctx, resp, 201);
  }

  public static RoleBasedAuthorization adminAuthor() {
    return RoleBasedAuthorization.create(Roles.ADMIN.toString());
  }
}
