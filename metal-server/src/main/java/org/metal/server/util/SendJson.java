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
