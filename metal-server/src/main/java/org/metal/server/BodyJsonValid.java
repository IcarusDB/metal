package org.metal.server;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class BodyJsonValid {
  private final static Logger LOGGER = LoggerFactory.getLogger(BodyJsonValid.class);

  public static void valid(RoutingContext ctx) {
    try {
      ctx.body().asJsonObject();
    } catch (DecodeException error) {
      LOGGER.error(error);
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", error.getLocalizedMessage());
      String payload = resp.toString();
      ctx.response()
          .setStatusCode(415)
          .putHeader("content-type", ctx.getAcceptableContentType())
          .putHeader("content-length", String.valueOf(payload.length()))
          .end(payload);
      return;
    }
    ctx.next();
  }

}
