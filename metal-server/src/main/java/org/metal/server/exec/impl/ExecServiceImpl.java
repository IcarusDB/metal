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

package org.metal.server.exec.impl;

import org.metal.server.api.ExecState;
import org.metal.server.exec.ExecDB;
import org.metal.server.exec.ExecService;
import org.metal.server.project.service.ProjectDB;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class ExecServiceImpl implements ExecService {

    private Vertx vertx;
    private MongoClient mongo;

    public ExecServiceImpl(Vertx vertx, MongoClient mongo, JsonObject conf) {
        this.vertx = vertx;
        this.mongo = mongo;
    }

    @Override
    public Future<String> add(String userId, JsonObject project) {
        return ExecDB.add(mongo, userId, project);
    }

    @Override
    public Future<JsonObject> remove(String userId, String execId) {
        return ExecDB.remove(mongo, userId, execId);
    }

    @Override
    public Future<JsonObject> forceRemove(String userId, String execId) {
        return ExecDB.forceRemove(mongo, userId, execId);
    }

    @Override
    public Future<JsonObject> removeAllOfUser(String userId) {
        return ExecDB.removeAllOfUser(mongo, userId);
    }

    @Override
    public Future<JsonObject> forceRemoveAllOfUser(String userId) {
        return ExecDB.forceRemoveAllOfUser(mongo, userId);
    }

    @Override
    public Future<JsonObject> removeAllOfProject(String userId, String projectId) {
        return ExecDB.removeAllOfProject(mongo, userId, projectId);
    }

    @Override
    public Future<JsonObject> forceRemoveAllOfProject(String userId, String projectId) {
        return ExecDB.forceRemoveAllOfProject(mongo, userId, projectId);
    }

    @Override
    public Future<JsonObject> removeAll() {
        return ExecDB.removeAll(mongo);
    }

    @Override
    public Future<JsonObject> forceRemoveAll() {
        return ExecDB.forceRemoveAll(mongo);
    }

    //  @Override
    //  public Future<Void> update(String execId, JsonObject update) {
    //    return null;
    //  }

    @Override
    public Future<Void> updateStatus(String execId, JsonObject execStatus) {
        String status = execStatus.getString("status");
        if (status == null) {
            IllegalArgumentException e =
                    new IllegalArgumentException(
                            String.format("%s lost status", execStatus.toString()));
            return Future.failedFuture(e);
        }

        try {
            ExecState.valueOf(status);
        } catch (IllegalArgumentException e) {
            return Future.failedFuture(e);
        }

        ExecState execState = ExecState.valueOf(status);
        JsonObject update = new JsonObject();
        update.put("status", status);

        switch (execState) {
            case CREATE:
                {
                    try {
                        Long createTime = execStatus.getLong("createTime");
                        if (createTime == null) {
                            return Future.failedFuture(
                                    String.format("%s lost createTime", execStatus.toString()));
                        }
                        update.put("createTime", createTime);
                    } catch (ClassCastException e) {
                        return Future.failedFuture(e);
                    }
                }
                ;
                break;
            case SUBMIT:
                {
                    try {
                        Long submitTime = execStatus.getLong("submitTime");
                        if (submitTime == null) {
                            return Future.failedFuture(
                                    String.format("%s lost submitTime", execStatus.toString()));
                        }
                        update.put("submitTime", submitTime);
                    } catch (ClassCastException e) {
                        return Future.failedFuture(e);
                    }
                }
                ;
                break;
            case RUNNING:
                {
                    try {
                        Long beatTime = execStatus.getLong("beatTime");
                        if (beatTime == null) {
                            return Future.failedFuture(
                                    String.format("%s lost beatTime", execStatus.toString()));
                        }
                        update.put("beatTime", beatTime);
                    } catch (ClassCastException e) {
                        return Future.failedFuture(e);
                    }
                }
                ;
                break;
            case FINISH:
                {
                    try {
                        Long finishTime = execStatus.getLong("finishTime");
                        if (finishTime == null) {
                            return Future.failedFuture(
                                    String.format("%s lost finishTime", execStatus.toString()));
                        }
                        update.put("finishTime", finishTime);
                    } catch (ClassCastException e) {
                        return Future.failedFuture(e);
                    }
                }
                ;
                break;
            case FAILURE:
                {
                    try {
                        Long terminateTime = execStatus.getLong("terminateTime");
                        if (terminateTime == null) {
                            return Future.failedFuture(
                                    String.format("%s lost terminateTime", execStatus.toString()));
                        }
                        update.put("terminateTime", terminateTime);
                        update.put("msg", execStatus.getString("msg"));
                    } catch (ClassCastException e) {
                        return Future.failedFuture(e);
                    }
                }
                ;
                break;
        }

        return ExecDB.updateStatus(mongo, execId, update)
                .compose(
                        ret -> {
                            return Future.succeededFuture();
                        });
    }

    @Override
    public Future<JsonObject> getStatus(String execId) {
        return ExecDB.getOfId(mongo, execId)
                .compose(
                        (JsonObject exec) -> {
                            JsonObject execStatus = new JsonObject();
                            JsonObject deploy = exec.getJsonObject(ExecDB.FIELD_DEPLOY);
                            String deployId = deploy.getString(ProjectDB.DEPLOY_ID);
                            int epoch = deploy.getInteger(ProjectDB.DEPLOY_EPOCH);

                            execStatus = exec.copy();
                            execStatus.remove(ExecDB.FIELD_SPEC);
                            execStatus.remove(ExecDB.FIELD_DEPLOY);
                            execStatus.put("deployId", deployId);
                            execStatus.put("epoch", epoch);
                            return Future.<JsonObject>succeededFuture(execStatus);
                        });
    }

    @Override
    public Future<JsonObject> getOfId(String execId) {
        return ExecDB.getOfId(mongo, execId);
    }

    @Override
    public Future<JsonObject> getOfIdNoDetail(String execId) {
        return ExecDB.getOfIdNoDetail(mongo, execId);
    }

    @Override
    public Future<List<JsonObject>> getAll() {
        return ExecDB.getAll(mongo);
    }

    @Override
    public Future<List<JsonObject>> getAllNoDetail() {
        return ExecDB.getAllNoDetail(mongo);
    }

    @Override
    public Future<List<JsonObject>> getAllOfUser(String userId) {
        return ExecDB.getAllOfUser(mongo, userId);
    }

    @Override
    public Future<List<JsonObject>> getAllOfUserNoDetail(String userId) {
        return ExecDB.getAllOfUserNoDetail(mongo, userId);
    }

    @Override
    public Future<List<JsonObject>> getAllOfProject(String projectId) {
        return ExecDB.getAllOfProject(mongo, projectId);
    }

    @Override
    public Future<List<JsonObject>> getAllOfProjectNoDetail(String projectId) {
        return ExecDB.getAllOfProjectNoDetail(mongo, projectId);
    }
}
