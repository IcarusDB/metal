/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.server.util;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RestServiceEnd {

  public static <T> void end(RoutingContext ctx, Future<T> result, Logger logger) {
    result.onSuccess((T ret) -> {
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
