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

package org.metal.server.repo.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;

@ProxyGen
@VertxGen
public interface IMetalRepoService {

  public static IMetalRepoService create(Vertx vertx, JsonObject conf) {
    String address = conf.getString("address");
    return new IMetalRepoServiceVertxEBProxy(vertx, address);
  }

  public static IMetalRepoService createProvider(Vertx vertx, MongoClient mongo, JsonObject conf) {
    return new MetalRepoServiceImpl(mongo, conf);
  }

  public Future<String> add(String userId, String type, String scope, JsonObject metal);

  public Future<JsonObject> get(String userId, String metalId);

  public Future<JsonObject> getOfClass(String userId, String clazz);

  public Future<List<JsonObject>> getAllOfClasses(String userId, List<String> clazzes);

  public Future<List<JsonObject>> getAllOfUser(String userId);

  public Future<List<JsonObject>> getAllOfUserScope(String userId, String scope);

  public Future<List<JsonObject>> getAllOfPublic();

  public Future<List<JsonObject>> getAllOfPkg(String userId, String groupId, String artifactId,
      String version);

  public Future<List<JsonObject>> getAllOfType(String userId, String type);

  public Future<JsonObject> addFromManifest(String userId, String scope, JsonObject manifest);

  public Future<JsonObject> removePrivate(String userId, String metalId);

  public Future<JsonObject> removeAllPrivateOfUser(String userId);

  public Future<JsonObject> removeAll();
}
