package org.metal.server.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.jwt.JWTAuth;

class ResultHandlerWrapper implements Handler<AsyncResult<User>> {
  private Handler<AsyncResult<User>> resultHandler;
  private AttachRoles attachRoles;

  private ResultHandlerWrapper(
      Handler<AsyncResult<User>> resultHandler,
      AttachRoles attachRoles
  ) {
    this.attachRoles = attachRoles;
    this.resultHandler = resultHandler;
  }

  public static ResultHandlerWrapper create(
      Handler<AsyncResult<User>> resultHandler,
      AttachRoles attachRoles
  ) {
    return new ResultHandlerWrapper(resultHandler, attachRoles);
  }

  @Override
  public void handle(AsyncResult<User> event) {
    if (event.succeeded()) {
      User user = event.result();
      attachRoles.roles(user).onComplete((AsyncResult<User> result) -> {
        resultHandler.handle(result);
      });
    } else {
      resultHandler.handle(event);
    }
  }
}


public class JWTAuthWithAttachRoles implements JWTAuth {
  private JWTAuth wrapped;
  private AttachRoles attachRoles;

  private JWTAuthWithAttachRoles(JWTAuth wrapped, AttachRoles attachRoles) {
    this.wrapped = wrapped;
    this.attachRoles = attachRoles;
  }

  public static JWTAuthWithAttachRoles create(JWTAuth wrapped, AttachRoles attachRoles) {
    return new JWTAuthWithAttachRoles(wrapped, attachRoles);
  }

  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    ResultHandlerWrapper resultHandlerWrapper = ResultHandlerWrapper.create(
        resultHandler,
        attachRoles
    );
    wrapped.authenticate(credentials, resultHandlerWrapper);
  }

  @Override
  public Future<User> authenticate(JsonObject credentials) {
    return wrapped.authenticate(credentials)
        .compose(attachRoles::roles);
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
    ResultHandlerWrapper resultHandlerWrapper = ResultHandlerWrapper.create(
        resultHandler,
        attachRoles
    );
    wrapped.authenticate(credentials, resultHandlerWrapper);
  }

  @Override
  public Future<User> authenticate(Credentials credentials) {
    return wrapped.authenticate(credentials)
        .compose(attachRoles::roles);
  }

  @Override
  public String generateToken(JsonObject claims, JWTOptions options) {
    return wrapped.generateToken(claims, options);
  }

  @Override
  public String generateToken(JsonObject claims) {
    return wrapped.generateToken(claims);
  }
}
