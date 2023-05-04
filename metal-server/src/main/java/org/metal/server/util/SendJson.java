package org.metal.server.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class SendJson {

  public static void send(RoutingContext ctx, JsonObject resp) {
    send(ctx, resp, 200);
  }

  public static void send(RoutingContext ctx, JsonObject resp, int statusCode) {
    String payload = resp.toString();
    ctx.response()
        .setStatusCode(statusCode)
        .putHeader("content-type", ctx.getAcceptableContentType())
        .putHeader("content-length", String.valueOf(payload.length()))
        .end(payload);
  }
}
