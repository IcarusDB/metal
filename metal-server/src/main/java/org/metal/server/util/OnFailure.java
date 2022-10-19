package org.metal.server.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.function.Supplier;

public class OnFailure {

  public static boolean doTry(RoutingContext ctx, Supplier<Boolean> condition, String msg, int code) {
    if (condition.get().booleanValue()) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL");
      resp.put("msg", msg);
      SendJson.send(ctx, resp, code);
      return true;
    } else {
      return false;
    }
  }
}
