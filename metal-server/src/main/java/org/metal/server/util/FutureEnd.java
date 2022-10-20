package org.metal.server.util;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FutureEnd {
  public static void end(RoutingContext ctx, Future<JsonObject> result, Logger logger) {
    result.onSuccess((JsonObject ret) -> {
      JsonObject resp = new JsonObject();
      resp.put("status", "OK")
          .put("data", ret);
      SendJson.send(ctx, resp, 200);
    }).onFailure((Throwable error) -> {
          JsonObject resp = new JsonObject();
          resp.put("status", "FAIL")
              .put("msg", error.getLocalizedMessage());
          SendJson.send(ctx, resp, 500);
          logger.error(error);
    });
  }
}
