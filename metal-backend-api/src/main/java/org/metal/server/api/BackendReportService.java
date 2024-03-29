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

package org.metal.server.api;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface BackendReportService {

    public static BackendReportService create(Vertx vertx, JsonObject config) {
        String address = config.getString("address");
        return new BackendReportServiceVertxEBProxy(vertx, address);
    }

    public Future<Void> reportExecSubmit(JsonObject create);

    public Future<Void> reportExecRunning(JsonObject running);

    public Future<Void> reportExecFinish(JsonObject finish);

    public Future<Void> reportExecFailure(JsonObject failure);

    public Future<Void> reportBackendUp(JsonObject up);

    public Future<Void> reportBackendDown(JsonObject down);

    public Future<Void> reportBackendFailure(JsonObject failure);
}
