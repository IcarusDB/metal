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

package org.metal.server.exec;

import org.metal.server.exec.impl.ExecServiceImpl;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

@ProxyGen
@VertxGen
public interface ExecService {

    public static ExecService create(Vertx vertx, JsonObject conf) {
        String address = conf.getString("address");
        return new ExecServiceVertxEBProxy(vertx, address);
    }

    public static ExecService createProvider(Vertx vertx, MongoClient mongo, JsonObject conf) {
        return new ExecServiceImpl(vertx, mongo, conf);
    }

    public Future<String> add(String userId, JsonObject project);

    public Future<JsonObject> remove(String userId, String execId);

    public Future<JsonObject> forceRemove(String userId, String execId);

    public Future<JsonObject> removeAllOfUser(String userId);

    public Future<JsonObject> forceRemoveAllOfUser(String userId);

    public Future<JsonObject> removeAllOfProject(String userId, String projectId);

    public Future<JsonObject> forceRemoveAllOfProject(String userId, String projectId);

    public Future<JsonObject> removeAll();

    public Future<JsonObject> forceRemoveAll();

    //    public Future<Void> update(String execId, JsonObject update);

    public Future<Void> updateStatus(String execId, JsonObject execStatus);

    public Future<JsonObject> getStatus(String execId);

    public Future<JsonObject> getOfId(String execId);

    public Future<JsonObject> getOfIdNoDetail(String execId);

    public Future<List<JsonObject>> getAll();

    public Future<List<JsonObject>> getAllNoDetail();

    public Future<List<JsonObject>> getAllOfUser(String userId);

    public Future<List<JsonObject>> getAllOfUserNoDetail(String userId);

    public Future<List<JsonObject>> getAllOfProject(String projectId);

    public Future<List<JsonObject>> getAllOfProjectNoDetail(String projectId);
}
