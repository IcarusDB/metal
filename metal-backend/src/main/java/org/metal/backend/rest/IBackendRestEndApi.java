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

package org.metal.backend.rest;

import io.vertx.ext.web.RoutingContext;
import org.metal.backend.api.BackendService;
import org.metal.backend.rest.impl.BackendRestEndApiImpl;

public interface IBackendRestEndApi {

  public static IBackendRestEndApi create(BackendService backendService) {
    return new BackendRestEndApiImpl(backendService);
  }

  public void analyseAPI(RoutingContext ctx);

  public void schemaAPI(RoutingContext ctx);

  public void heartAPI(RoutingContext ctx);

  public void statusAPI(RoutingContext ctx);

  public void execAPI(RoutingContext ctx);
}
