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

package org.metal.server.repo;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.ext.mongo.MongoClientDeleteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetalRepoDB {

    public static enum MetalType {
        SOURCE,
        MAPPER,
        FUSION,
        SINK,
        SETUP
    }

    public static enum MetalScope {
        PUBLIC,
        PRIVATE
    }

    public static final String DB = "metals";

    private static JsonObject parsePkg(String pkg) throws IllegalArgumentException {
        String[] s = pkg.strip().split(":");
        if (s.length != 3) {
            throw new IllegalArgumentException(
                    "Fail to parse groupId:artifactId:version from " + pkg);
        }

        return new JsonObject().put("groupId", s[0]).put("artifactId", s[1]).put("version", s[2]);
    }

    private static JsonObject metalOf(
            String userId,
            MetalType type,
            MetalScope scope,
            String pkg,
            String clazz,
            JsonObject formSchema,
            Optional<JsonObject> uiSchema) {
        JsonObject metal =
                new JsonObject()
                        .put("userId", userId)
                        .put("type", type.toString())
                        .put("scope", scope.toString())
                        .put("createTime", System.currentTimeMillis())
                        .put("pkg", pkg)
                        .put("class", clazz)
                        .put("formSchema", formSchema);

        try {
            JsonObject pkgInfo = parsePkg(pkg);
            metal.mergeIn(pkgInfo);
        } catch (IllegalArgumentException e) {
        }

        uiSchema.ifPresent(
                u -> {
                    metal.put("uiSchema", u);
                });
        return metal;
    }

    public static Future<String> add(
            MongoClient mongo,
            String userId,
            MetalType type,
            MetalScope scope,
            String pkg,
            String clazz,
            JsonObject formSchema,
            Optional<JsonObject> uiSchema) {
        JsonObject metal = metalOf(userId, type, scope, pkg, clazz, formSchema, uiSchema);
        return mongo.insert(DB, metal);
    }

    private static BulkOperation wrapBulkOperation(
            JsonObject metalRaw, String userId, MetalType type, MetalScope scope) {
        JsonObject metal =
                metalOf(
                        userId,
                        type,
                        scope,
                        metalRaw.getString("pkg"),
                        metalRaw.getString("class"),
                        metalRaw.getJsonObject("formSchema"),
                        Optional.ofNullable(metalRaw.getJsonObject("uiSchema")));
        return BulkOperation.createInsert(metal);
    }

    public static Future<MongoClientBulkWriteResult> addFromManifest(
            MongoClient mongo, String userId, MetalScope scope, JsonObject manifest) {
        List<BulkOperation> operations = new ArrayList<>();
        JsonArray sources = manifest.getJsonArray("sources");
        sources.stream()
                .map(obj -> (JsonObject) obj)
                .map(
                        (JsonObject metalRaw) -> {
                            return wrapBulkOperation(metalRaw, userId, MetalType.SOURCE, scope);
                        })
                .forEach(operations::add);

        JsonArray mappers = manifest.getJsonArray("mappers");
        mappers.stream()
                .map(obj -> (JsonObject) obj)
                .map(
                        (JsonObject metalRaw) -> {
                            return wrapBulkOperation(metalRaw, userId, MetalType.MAPPER, scope);
                        })
                .forEach(operations::add);

        JsonArray fusions = manifest.getJsonArray("fusions");
        fusions.stream()
                .map(obj -> (JsonObject) obj)
                .map(
                        (JsonObject metalRaw) -> {
                            return wrapBulkOperation(metalRaw, userId, MetalType.FUSION, scope);
                        })
                .forEach(operations::add);

        JsonArray sinks = manifest.getJsonArray("sinks");
        sinks.stream()
                .map(obj -> (JsonObject) obj)
                .map(
                        (JsonObject metalRaw) -> {
                            return wrapBulkOperation(metalRaw, userId, MetalType.SINK, scope);
                        })
                .forEach(operations::add);

        JsonArray setups = manifest.getJsonArray("setups");
        setups.stream()
                .map(obj -> (JsonObject) obj)
                .map(
                        (JsonObject metalRaw) -> {
                            return wrapBulkOperation(metalRaw, userId, MetalType.SETUP, scope);
                        })
                .forEach(operations::add);

        return mongo.bulkWrite(DB, operations);
    }

    public static Future<JsonObject> get(MongoClient mongo, String userId, String metalId) {
        JsonObject userPrivate = new JsonObject().put("userId", userId);
        JsonObject publicAccess = new JsonObject().put("scope", MetalScope.PUBLIC.toString());
        JsonObject query =
                new JsonObject()
                        .put("_id", metalId)
                        .put("$or", new JsonArray().add(userPrivate).add(publicAccess));
        return mongo.findOne(DB, query, new JsonObject());
    }

    public static Future<JsonObject> getOfClass(MongoClient mongo, String userId, String clazz) {
        JsonObject userPrivate = new JsonObject().put("userId", userId);
        JsonObject publicAccess = new JsonObject().put("scope", MetalScope.PUBLIC.toString());
        JsonObject query =
                new JsonObject()
                        .put("$or", new JsonArray().add(userPrivate).add(publicAccess))
                        .put("class", clazz);
        return mongo.findOne(DB, query, new JsonObject());
    }

    public static Future<List<JsonObject>> getAllOfClasses(
            MongoClient mongo, String userId, List<String> clazzes) {
        JsonObject userPrivate = new JsonObject().put("userId", userId);
        JsonObject publicAccess = new JsonObject().put("scope", MetalScope.PUBLIC.toString());
        JsonArray classQuery = new JsonArray();
        if (clazzes != null) {
            for (String clazz : clazzes) {
                classQuery.add(new JsonObject().put("class", clazz));
            }
        }
        JsonObject query =
                new JsonObject()
                        .put("$or", new JsonArray().add(userPrivate).add(publicAccess))
                        .put("$or", classQuery);
        return mongo.find(DB, query);
    }

    public static Future<List<JsonObject>> getAllOfPkg(
            MongoClient mongo,
            String userId,
            String groupId,
            Optional<String> artifactId,
            Optional<String> version) {
        JsonObject userPrivate = new JsonObject().put("userId", userId);
        JsonObject publicAccess = new JsonObject().put("scope", MetalScope.PUBLIC.toString());
        JsonObject query =
                new JsonObject().put("$or", new JsonArray().add(userPrivate).add(publicAccess));
        query.put("groupId", groupId);
        if (artifactId.isPresent() && !artifactId.get().isBlank()) {
            query.put("artifactId", artifactId.get());
            if (version.isPresent() && !version.get().isBlank()) {
                query.put("version", version.get());
            }
        }

        return mongo.find(DB, query);
    }

    public static Future<List<JsonObject>> getAllOfType(
            MongoClient mongo, String userId, MetalType type) {
        JsonObject userPrivate = new JsonObject().put("userId", userId);
        JsonObject publicAccess = new JsonObject().put("scope", MetalScope.PUBLIC.toString());
        JsonObject query =
                new JsonObject().put("$or", new JsonArray().add(userPrivate).add(publicAccess));
        query.put("type", type.toString());
        return mongo.find(DB, query);
    }

    public static ReadStream<JsonObject> getAllPrivateOfUser(MongoClient mongo, String userId) {
        return mongo.findBatch(DB, new JsonObject().put("userId", userId));
    }

    public static ReadStream<JsonObject> getAllOfUserScope(
            MongoClient mongo, String userId, MetalScope scope) {
        return mongo.findBatch(
                DB, new JsonObject().put("userId", userId).put("scope", scope.toString()));
    }

    public static ReadStream<JsonObject> getAllOfPublic(MongoClient mongo) {
        return mongo.findBatch(DB, new JsonObject().put("scope", MetalScope.PUBLIC.toString()));
    }

    public static Future<MongoClientDeleteResult> removePrivate(
            MongoClient mongo, String userId, String metalId) {
        return mongo.removeDocument(
                DB,
                new JsonObject()
                        .put("scope", MetalScope.PRIVATE.toString())
                        .put("_id", metalId)
                        .put("userId", userId));
    }

    public static Future<MongoClientDeleteResult> removeAllPrivateOfUser(
            MongoClient mongo, String userId) {
        return mongo.removeDocuments(
                DB,
                new JsonObject().put("scope", MetalScope.PRIVATE.toString()).put("userId", userId));
    }

    public static Future<MongoClientDeleteResult> removeAll(MongoClient mongo) {
        return mongo.removeDocuments(DB, new JsonObject());
    }
}
