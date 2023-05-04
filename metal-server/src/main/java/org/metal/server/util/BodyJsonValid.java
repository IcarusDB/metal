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
