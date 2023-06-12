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

package org.metal.server.project.service;

import org.metal.server.api.BackendState;
import org.metal.server.exec.ExecDB;
import org.metal.server.user.UserDB;
import org.metal.server.util.JsonConvertor;
import org.metal.server.util.JsonKeyReplacer;
import org.metal.server.util.ReadStreamCollector;
import org.metal.server.util.SpecJson;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProjectDB {

    public static final String DB = "project";
    public static final String NAME = "name";
    public static final String CREATE_TIME = "createTime";
    public static final String DEPLOY = "deploy";
    public static final String DEPLOY_ID = "id";
    public static final String DEPLOY_PLATFORM = "platform";
    public static final String DEPLOY_BACKEND = "backend";
    public static final String SPEC = "spec";
    public static final String DEPLOY_BACKEND_STATUS = "status";
    public static final String DEPLOY_BACKEND_STATUS_CURRENT = "current";
    public static final String DEPLOY_BACKEND_ARGS = "args";
    public static final String DEPLOY_EPOCH = "epoch";
    public static final String DEPLOY_PKGS = "pkgs";
    public static final int DEPLOY_EPOCH_DEFAULT = 0;
    public static final String USER_REF = "userRef";
    public static final String USER_REF_REF = "$ref";
    public static final String USER_REF_ID = "$id";
    public static final String ID = "_id";
    public static final String USER = "user";

    public static final String DEPLOY_BACKEND_STATUS_CREATED_TIME = "createdTime";
    public static final String DEPLOY_BACKEND_STATUS_UP_TIME = "upTime";
    public static final String DEPLOY_BACKEND_STATUS_DOWN_TIME = "downTime";
    public static final String DEPLOY_BACKEND_STATUS_FAILURE_TIME = "failureTime";
    public static final String DEPLOY_BACKEND_STATUS_FAILURE_MSG = "failureMsg";
    public static final String DEPLOY_BACKEND_STATUS_TRACER = "tracer";

    public static Future<String> add(
            MongoClient mongo,
            String userId,
            String name,
            List<String> pkgs,
            JsonObject platform,
            List<String> backendArgs,
            JsonObject spec) {
        JsonObject project = new JsonObject();
        JsonObject userRef = new JsonObject();
        JsonObject deploy = new JsonObject();
        JsonObject backend = new JsonObject();

        if (platform != null) {
            platform = JsonKeyReplacer.compatBson(platform);
        }

        userRef.put(USER_REF_REF, UserDB.DB).put(USER_REF_ID, userId);
        project.put(USER_REF, userRef);
        project.put(NAME, name);
        project.put(CREATE_TIME, getTime());
        project.put(DEPLOY, deploy);
        deploy.put(DEPLOY_ID, generateDeployId());
        deploy.put(DEPLOY_EPOCH, DEPLOY_EPOCH_DEFAULT);
        deploy.put(DEPLOY_PKGS, pkgs);
        deploy.put(DEPLOY_PLATFORM, platform);
        deploy.put(DEPLOY_BACKEND, backend);
        backend.put(DEPLOY_BACKEND_ARGS, backendArgs);
        backend.put(
                DEPLOY_BACKEND_STATUS,
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.DOWN.toString())
                        .put(DEPLOY_BACKEND_STATUS_DOWN_TIME, getTime()));
        project.put(SPEC, spec);

        return mongo.insert(DB, project);
    }

    private static String generateDeployId() {
        return UUID.randomUUID().toString();
    }

    public static Future<String> copyFromProject(MongoClient mongo, String userId, String name) {
        String newName = name + "-copy-" + UUID.randomUUID().toString().substring(0, 8);
        return copyFromProject(mongo, userId, name, newName);
    }

    public static Future<String> copyFromProject(
            MongoClient mongo, String userId, String name, String copyName) {
        JsonObject matcher = new JsonObject();
        matcher.put(userIdPath(), userId).put(NAME, name);

        return mongo.findOne(DB, matcher, new JsonObject())
                .compose(
                        project -> {
                            project.remove(ID);
                            project.put(NAME, copyName);
                            project.put(CREATE_TIME, getTime());

                            int epoch = DEPLOY_EPOCH_DEFAULT;
                            String deployId = generateDeployId();
                            JsonObject deploy = project.getJsonObject(DEPLOY);
                            deploy.put(DEPLOY_ID, deployId);
                            deploy.put(DEPLOY_EPOCH, epoch);

                            JsonObject backend = deploy.getJsonObject(DEPLOY_BACKEND);
                            backend.remove(DEPLOY_BACKEND_STATUS);
                            backend.put(
                                    DEPLOY_BACKEND_STATUS,
                                    new JsonObject()
                                            .put(
                                                    DEPLOY_BACKEND_STATUS_CURRENT,
                                                    BackendState.DOWN.toString())
                                            .put(DEPLOY_BACKEND_STATUS_DOWN_TIME, getTime()));
                            return Future.succeededFuture(project);
                        })
                .compose(
                        project -> {
                            return mongo.insert(DB, project);
                        });
    }

    public static Future<String> recoverFromExec(MongoClient mongo, String userId, String execId) {
        String name = "recover-from-exec[" + execId + "]";

        return ExecDB.getOfId(mongo, execId)
                .compose(
                        (JsonObject exec) -> {
                            if (exec == null || exec.isEmpty()) {
                                return Future.failedFuture("No exec[" + execId + "] found.");
                            }
                            try {
                                JsonObject spec = exec.getJsonObject(ExecDB.FIELD_SPEC);
                                if (spec == null || spec.isEmpty() || !SpecJson.check(spec)) {
                                    return Future.failedFuture("No spec found.");
                                }

                                JsonObject deploy = exec.getJsonObject(ExecDB.FIELD_DEPLOY);
                                if (deploy == null || deploy.isEmpty()) {
                                    return Future.failedFuture("No deploy found");
                                }

                                JsonObject platform = deploy.getJsonObject(DEPLOY_PLATFORM);
                                if (platform == null || platform.isEmpty()) {
                                    return Future.failedFuture("No platform found.");
                                }

                                JsonObject backend = deploy.getJsonObject(DEPLOY_BACKEND);
                                if (backend == null || backend.isEmpty()) {
                                    return Future.failedFuture("No backend found.");
                                }

                                List<String> pkgs =
                                        JsonConvertor.jsonArrayToList(
                                                deploy.getJsonArray(DEPLOY_PKGS));

                                List<String> backendArgs =
                                        JsonConvertor.jsonArrayToList(
                                                backend.getJsonArray(DEPLOY_BACKEND_ARGS));

                                return add(mongo, userId, name, pkgs, platform, backendArgs, spec);
                            } catch (Exception e) {
                                return Future.failedFuture(e);
                            }
                        });
    }

    public static Future<List<JsonObject>> getAllOfMatcher(MongoClient mongo, JsonObject matcher) {
        JsonObject match = new JsonObject();
        JsonObject lookup = new JsonObject();
        JsonObject project = new JsonObject();
        JsonObject privateProtect = new JsonObject();

        match.put("$match", matcher);

        lookup.put(
                "$lookup",
                new JsonObject()
                        .put("from", UserDB.DB)
                        .put("localField", userIdPath())
                        .put("foreignField", UserDB.FIELD_ID)
                        .put("as", USER));

        project.put(
                "$project",
                new JsonObject()
                        .put(ID, true)
                        .put(NAME, true)
                        .put(SPEC, true)
                        .put(DEPLOY, true)
                        .put(
                                USER,
                                new JsonObject()
                                        .put(
                                                "$arrayElemAt",
                                                new JsonArray().add("$" + USER).add(0))));

        privateProtect = project.copy();
        privateProtect
                .getJsonObject("$project")
                .put(
                        USER,
                        new JsonObject()
                                .put(UserDB.FIELD_ID, true)
                                .put(UserDB.FIELD_USER_NAME, true));

        JsonArray pipeline =
                new JsonArray().add(match).add(lookup).add(project).add(privateProtect);
        return ReadStreamCollector.<JsonObject, JsonObject>toList(
                mongo.aggregate(DB, pipeline), ProjectDB::compatJsonOnPlatform);
    }

    private static JsonObject compatJsonOnPlatform(JsonObject proj) {
        JsonObject platform = proj.getJsonObject(DEPLOY).getJsonObject(DEPLOY_PLATFORM);
        platform = JsonKeyReplacer.compatJson(platform);
        proj.getJsonObject(DEPLOY).put(DEPLOY_PLATFORM, platform);
        return proj;
    }

    public static Future<JsonObject> getOfMatcher(MongoClient mongo, JsonObject matcher) {
        return getAllOfMatcher(mongo, matcher)
                .compose(
                        projects -> {
                            try {
                                return Future.succeededFuture(projects.get(0));
                            } catch (IndexOutOfBoundsException e) {
                                return Future.succeededFuture(new JsonObject());
                            }
                        });
    }

    public static Future<JsonObject> getOfId(MongoClient mongo, String userId, String projectId) {
        JsonObject matcher = new JsonObject();
        matcher.put(ID, projectId).put(userIdPath(), userId);
        return getOfMatcher(mongo, matcher);
    }

    public static Future<JsonObject> getOfName(
            MongoClient mongo, String userId, String projectName) {
        JsonObject matcher = new JsonObject();
        matcher.put(NAME, projectName).put(userIdPath(), userId);
        return getOfMatcher(mongo, matcher);
    }

    public static Future<List<JsonObject>> getAllOfUser(MongoClient mongo, String userId) {
        JsonObject matcher = new JsonObject();
        matcher.put(userIdPath(), userId);
        return getAllOfMatcher(mongo, matcher);
    }

    public static Future<List<JsonObject>> getAll(MongoClient mongo) {
        return getAllOfMatcher(mongo, new JsonObject());
    }

    public static Future<JsonObject> getBackendStatus(MongoClient mongo, String deployId) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId);
        return mongo.findOne(DB, matcher, new JsonObject())
                .compose(
                        proj -> {
                            return wrapBackendStatus(deployId, Optional.empty(), proj);
                        });
    }

    public static Future<JsonObject> getBackendStatus(
            MongoClient mongo, String deployId, int epoch) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId).put(epochPath(), epoch);
        return mongo.findOne(DB, matcher, new JsonObject())
                .compose(
                        proj -> {
                            return wrapBackendStatus(deployId, Optional.of(epoch), proj);
                        });
    }

    private static Future<JsonObject> wrapBackendStatus(
            String deployId, Optional<Integer> epoch, JsonObject proj) {
        String epochVal = epoch.isPresent() ? epoch.get().toString() : "*";
        if (proj == null) {
            return Future.failedFuture(
                    "Backend[" + deployId + "-" + epochVal + "] is not deployed.");
        }
        JsonObject deploy = proj.getJsonObject(DEPLOY);
        JsonObject status =
                deploy.getJsonObject(DEPLOY_BACKEND)
                        .getJsonObject(DEPLOY_BACKEND_STATUS, new JsonObject());
        status.put("deployId", deploy.getValue(DEPLOY_ID));
        status.put("epoch", deploy.getValue(DEPLOY_EPOCH));
        return Future.succeededFuture(status);
    }

    public static Future<JsonObject> getBackendTracer(MongoClient mongo, String deployId) {
        return getBackendStatus(mongo, deployId)
                .compose(
                        (JsonObject backendStatus) -> {
                            JsonObject tracer =
                                    backendStatus.getJsonObject(DEPLOY_BACKEND_STATUS_TRACER);
                            return Future.succeededFuture(tracer);
                        });
    }

    public static Future<JsonObject> getDeployOfDeployId(MongoClient mongo, String deployId) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId);
        return getOfMatcher(mongo, matcher)
                .compose(
                        proj -> {
                            JsonObject deploy = proj.getJsonObject(DEPLOY);
                            return Future.succeededFuture(deploy);
                        });
    }

    public static Future<JsonObject> getDeployOfDeployIdWithEpoch(
            MongoClient mongo, String deployId, int epoch) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId).put(epochPath(), epoch);
        return getOfMatcher(mongo, matcher)
                .compose(
                        proj -> {
                            JsonObject deploy = proj.getJsonObject(DEPLOY);
                            return Future.succeededFuture(deploy);
                        });
    }

    public static Future<JsonObject> getDeployAddress(MongoClient mongo, String deployId) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId);
        return getOfMatcher(mongo, matcher)
                .compose(
                        proj -> {
                            JsonObject deploy = proj.getJsonObject(DEPLOY);
                            int epoch = deploy.getInteger(DEPLOY_EPOCH);
                            String address = deployId + "-" + epoch;
                            return Future.succeededFuture(new JsonObject().put("address", address));
                        });
    }

    public static Future<JsonObject> getDeployAddressOfName(
            MongoClient mongo, String userId, String name) {
        JsonObject matcher = new JsonObject();
        matcher.put(userIdPath(), userId).put(NAME, name);
        return getOfMatcher(mongo, matcher)
                .compose(
                        proj -> {
                            JsonObject deploy = proj.getJsonObject(DEPLOY);
                            int epoch = deploy.getInteger(DEPLOY_EPOCH);
                            String deployId = deploy.getString(DEPLOY_ID);
                            String address = deployId + "-" + epoch;
                            return Future.succeededFuture(new JsonObject().put("address", address));
                        });
    }

    public static Future<JsonObject> getSpecOfName(MongoClient mongo, String userId, String name) {
        JsonObject matcher = new JsonObject();
        matcher.put(userIdPath(), userId).put(NAME, name);
        return getOfMatcher(mongo, matcher)
                .compose(
                        proj -> {
                            if (proj == null || proj.isEmpty()) {
                                return Future.succeededFuture(SpecJson.empty());
                            }
                            JsonObject spec = proj.getJsonObject(SPEC);
                            try {
                                SpecJson.check(spec);
                                return Future.succeededFuture(spec);
                            } catch (IllegalArgumentException e) {
                                return Future.failedFuture(e);
                            }
                        });
    }

    public static Future<JsonObject> update(
            MongoClient mongo, JsonObject matcher, JsonObject updater) {
        return mongo.updateCollection(DB, matcher, updater)
                .compose(
                        result -> {
                            if (result.getDocMatched() == 0) {
                                return Future.failedFuture(
                                        String.format(
                                                "Fail to match any record. Matcher: %s",
                                                matcher.toString()));
                            }
                            return Future.succeededFuture(result.toJson());
                        });
    }

    public static Future<JsonObject> update(
            MongoClient mongo, String userId, String name, JsonObject updater) {
        JsonObject matcher = new JsonObject();
        matcher.put(NAME, name).put(userIdPath(), userId);

        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateProject(
            MongoClient mongo,
            String userId,
            String id,
            String name,
            List<String> pkgs,
            JsonObject platform,
            List<String> backendArgs,
            JsonObject spec) {
        JsonObject matcher = new JsonObject();
        matcher.put(ID, id).put(userIdPath(), userId);

        JsonObject updater = new JsonObject();
        if (name != null && !name.isBlank()) {
            updater.put(NAME, name);
        }
        if (pkgs != null) {
            updater.put(pkgsPath(), pkgs);
        }
        if (platform != null && !platform.isEmpty()) {
            updater.put(platformPath(), platform);
        }
        if (backendArgs != null) {
            updater.put(backendArgsPath(), backendArgs);
        }
        if (spec != null && !spec.isEmpty()) {
            updater.put(SPEC, spec);
        }

        return update(mongo, matcher, new JsonObject().put("$set", updater));
    }

    public static Future<JsonObject> updateOnDeployUnlock(
            MongoClient mongo, String userId, String name, JsonObject updater) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(NAME, name).put(userIdPath(), userId);

        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateName(
            MongoClient mongo, String userId, String name, String newName) {
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(NAME, newName));
        return update(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updateSpec(
            MongoClient mongo, String userId, String name, JsonObject spec) {
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(SPEC, spec));
        return update(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updatePlatform(
            MongoClient mongo, String userId, String name, JsonObject platform) {
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(platformPath(), platform));
        return updateOnDeployUnlock(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updatePlatform(
            MongoClient mongo, String deployId, JsonObject platform) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(platformPath(), platform));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updatePkgs(
            MongoClient mongo, String userId, String name, List<String> pkgs) {
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(pkgsPath(), pkgs));
        return updateOnDeployUnlock(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updatePkgs(
            MongoClient mongo, String deployId, List<String> pkgs) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(pkgsPath(), pkgs));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateBackendArgs(
            MongoClient mongo, String userId, String name, List<String> backendArgs) {
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(backendArgsPath(), backendArgs));
        return updateOnDeployUnlock(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updateBackendArgs(
            MongoClient mongo, String deployId, List<String> backendArgs) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        updater.put("$set", new JsonObject().put(backendArgsPath(), backendArgs));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateBackendStatus(
            MongoClient mongo, String deployId, JsonObject status) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject().put("$set", confWithStatusPath(status));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateBackendStatus(
            MongoClient mongo, String deployId, int epoch, JsonObject status) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId).put(epochPath(), epoch);
        JsonObject updater = new JsonObject().put("$set", confWithStatusPath(status));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateBackendStatus(
            MongoClient mongo,
            String deployId,
            int epoch,
            BackendState current,
            JsonObject status) {
        JsonObject matcher = new JsonObject();
        matcher.put(deployIdPath(), deployId)
                .put(epochPath(), epoch)
                .put(backendStatusCurrentPath(), current.toString());
        JsonObject updater = new JsonObject().put("$set", confWithStatusPath(status));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> updateBackendStatusOnUndeploy(
            MongoClient mongo, String deployId) {
        JsonObject undeploy =
                new JsonObject().put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.DOWN.toString());
        return updateBackendStatus(mongo, deployId, undeploy);
    }

    public static Future<JsonObject> updateBackendStatusOnCreated(
            MongoClient mongo, String deployId) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.CREATED.toString())
                        .put(DEPLOY_BACKEND_STATUS_CREATED_TIME, getTime());
        return updateBackendStatus(mongo, deployId, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnCreated(
            MongoClient mongo, String deployId, int epoch, BackendState current) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.CREATED.toString())
                        .put(DEPLOY_BACKEND_STATUS_CREATED_TIME, getTime());
        return updateBackendStatus(mongo, deployId, epoch, current, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnUp(MongoClient mongo, String deployId) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.UP.toString())
                        .put(DEPLOY_BACKEND_STATUS_UP_TIME, getTime());
        return updateBackendStatus(mongo, deployId, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnUp(
            MongoClient mongo, String deployId, int epoch, BackendState current) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.UP.toString())
                        .put(DEPLOY_BACKEND_STATUS_UP_TIME, getTime());
        return updateBackendStatus(mongo, deployId, epoch, current, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnDown(MongoClient mongo, String deployId) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.DOWN.toString())
                        .put(DEPLOY_BACKEND_STATUS_DOWN_TIME, getTime());
        return updateBackendStatus(mongo, deployId, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnDown(
            MongoClient mongo, String deployId, int epoch, BackendState current) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.DOWN.toString())
                        .put(DEPLOY_BACKEND_STATUS_DOWN_TIME, getTime());
        return updateBackendStatus(mongo, deployId, epoch, current, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnFailure(
            MongoClient mongo, String deployId, String failureMsg) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.FAILURE.toString())
                        .put(DEPLOY_BACKEND_STATUS_FAILURE_TIME, getTime())
                        .put(DEPLOY_BACKEND_STATUS_FAILURE_MSG, failureMsg);
        return updateBackendStatus(mongo, deployId, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusOnFailure(
            MongoClient mongo,
            String deployId,
            int epoch,
            BackendState current,
            String failureMsg) {
        JsonObject newStatus =
                new JsonObject()
                        .put(DEPLOY_BACKEND_STATUS_CURRENT, BackendState.FAILURE.toString())
                        .put(DEPLOY_BACKEND_STATUS_FAILURE_TIME, getTime())
                        .put(DEPLOY_BACKEND_STATUS_FAILURE_MSG, failureMsg);
        return updateBackendStatus(mongo, deployId, epoch, current, newStatus);
    }

    public static Future<JsonObject> updateBackendStatusTracer(
            MongoClient mongo, String deployId, JsonObject tracer) {
        JsonObject undeploy = new JsonObject().put(DEPLOY_BACKEND_STATUS_TRACER, tracer);
        return updateBackendStatus(mongo, deployId, undeploy);
    }

    public static Future<JsonObject> updateBackendStatusTracer(
            MongoClient mongo, String deployId, int epoch, JsonObject tracer) {
        JsonObject undeploy = new JsonObject().put(DEPLOY_BACKEND_STATUS_TRACER, tracer);
        return updateBackendStatus(mongo, deployId, epoch, undeploy);
    }

    public static Future<JsonObject> updateDeployConfs(
            MongoClient mongo, String userId, String name, JsonObject confs) {
        JsonObject updater = new JsonObject();
        JsonObject confsWithPath = deployConfsWithPath(confs);
        updater.put("$set", confsWithPath);
        return updateOnDeployUnlock(mongo, userId, name, updater);
    }

    public static Future<JsonObject> updateDeployConfs(
            MongoClient mongo, String deployId, JsonObject confs) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        JsonObject confsWithPath = deployConfsWithPath(confs);
        updater.put("$set", confsWithPath);
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> increaseDeployEpoch(
            MongoClient mongo, String userId, String name) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(userIdPath(), userId).put(NAME, name);
        JsonObject updater = new JsonObject();
        JsonObject confsWithPath = deployConfsWithPath(new JsonObject().put(DEPLOY_EPOCH, 1));
        updater.put("$inc", confsWithPath);
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> increaseDeployEpoch(MongoClient mongo, String deployId) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        JsonObject confsWithPath = deployConfsWithPath(new JsonObject().put(DEPLOY_EPOCH, 1));
        updater.put("$inc", confsWithPath);
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> increaseDeployEpoch(
            MongoClient mongo, String deployId, int epoch, BackendState current) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(deployIdPath(), deployId)
                .put(epochPath(), epoch)
                .put(backendStatusCurrentPath(), current.toString());
        JsonObject updater = new JsonObject();
        JsonObject confsWithPath = deployConfsWithPath(new JsonObject().put(DEPLOY_EPOCH, 1));
        updater.put("$inc", confsWithPath)
                .put(
                        "$set",
                        new JsonObject()
                                .put(backendStatusCurrentPath(), BackendState.CREATED)
                                .put(backendStatusCreateTimePath(), getTime()));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> removeBackendStatusOfDeployId(
            MongoClient mongo, String deployId) {
        JsonObject matcher = deployIsLock();
        matcher.put(deployIdPath(), deployId);
        JsonObject updater = new JsonObject();
        updater.put("$unset", new JsonObject().put(backendStatusPath(), true));
        return update(mongo, matcher, updater);
    }

    public static Future<JsonObject> removeOfId(MongoClient mongo, String userId, String id) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(userIdPath(), userId).put(ID, id);
        return remove(mongo, matcher);
    }

    public static Future<JsonObject> removeOfName(MongoClient mongo, String userId, String name) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(userIdPath(), userId).put(NAME, name);
        return remove(mongo, matcher);
    }

    public static Future<JsonObject> removeAllOfUser(MongoClient mongo, String userId) {
        JsonObject matcher = deployIsUnlock();
        matcher.put(userIdPath(), userId);
        return remove(mongo, matcher);
    }

    public static Future<JsonObject> removeAll(MongoClient mongo) {
        JsonObject matcher = deployIsUnlock();
        return remove(mongo, matcher);
    }

    public static Future<JsonObject> remove(MongoClient mongo, JsonObject matcher) {
        return mongo.removeDocuments(DB, matcher)
                .compose(
                        result -> {
                            return Future.succeededFuture(result.toJson());
                        });
    }

    private static JsonObject deployConfsWithPath(JsonObject confs) {
        JsonObject confsWithPath = new JsonObject();
        for (String field : confs.fieldNames()) {
            confsWithPath.put(DEPLOY + "." + field, confs.getValue(field));
        }
        return confsWithPath;
    }

    private static JsonObject confWithStatusPath(JsonObject confs) {
        JsonObject confsWithPath = new JsonObject();
        for (String field : confs.fieldNames()) {
            confsWithPath.put(backendStatusPath() + "." + field, confs.getValue(field));
        }
        return confsWithPath;
    }

    public static JsonObject deployIsUnlock() {
        return new JsonObject()
                .put(
                        "$or",
                        new JsonArray()
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.DOWN.toString()))
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.FAILURE.toString())));
    }

    public static JsonObject deployIsLock() {
        return new JsonObject()
                .put(
                        "$or",
                        new JsonArray()
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.CREATED.toString()))
                                .add(
                                        new JsonObject()
                                                .put(
                                                        backendStatusCurrentPath(),
                                                        BackendState.UP.toString())));
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static String userIdPath() {
        return USER_REF + "." + USER_REF_ID;
    }

    public static String deployIdPath() {
        return DEPLOY + "." + DEPLOY_ID;
    }

    public static String epochPath() {
        return DEPLOY + "." + DEPLOY_EPOCH;
    }

    public static String platformPath() {
        return DEPLOY + "." + DEPLOY_PLATFORM;
    }

    public static String backendArgsPath() {
        return DEPLOY + "." + DEPLOY_BACKEND + "." + DEPLOY_BACKEND_ARGS;
    }

    public static String backendStatusPath() {
        return DEPLOY + "." + DEPLOY_BACKEND + "." + DEPLOY_BACKEND_STATUS;
    }

    public static String backendStatusCurrentPath() {
        return DEPLOY
                + "."
                + DEPLOY_BACKEND
                + "."
                + DEPLOY_BACKEND_STATUS
                + "."
                + DEPLOY_BACKEND_STATUS_CURRENT;
    }

    public static String backendStatusCreateTimePath() {
        return DEPLOY
                + "."
                + DEPLOY_BACKEND
                + "."
                + DEPLOY_BACKEND_STATUS
                + "."
                + DEPLOY_BACKEND_STATUS_CREATED_TIME;
    }

    public static String backendStatusUpTimePath() {
        return DEPLOY
                + "."
                + DEPLOY_BACKEND
                + "."
                + DEPLOY_BACKEND_STATUS
                + "."
                + DEPLOY_BACKEND_STATUS_UP_TIME;
    }

    public static String pkgsPath() {
        return DEPLOY + "." + DEPLOY_PKGS;
    }
}
