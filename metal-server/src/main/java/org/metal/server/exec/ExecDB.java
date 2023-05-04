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

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import org.metal.server.api.ExecState;
import org.metal.server.project.service.ProjectDB;
import org.metal.server.util.JsonKeyReplacer;
import org.metal.server.util.ReadStreamCollector;

public class ExecDB {

  public final static String DB = "execs";
  public final static String FIELD_ID = "_id";
  public final static String FIELD_USER_ID = "userId";
  public final static String FIELD_CREATE_TIME = "createTime";
  public final static String FIELD_SUBMIT_TIME = "submitTime";
  public final static String FIELD_BEAT_TIME = "beatTime";
  public final static String FIELD_FINISH_TIME = "finishTime";
  public final static String FIELD_TERMINATE_TIME = "terminateTime";
  public final static String FIELD_STATUS = "status";

  public final static String FIELD_DEPLOY = "deploy";
  public final static String FIELD_FROM_PROJECT = "fromProject";
  public final static String FIELD_SPEC = "SPEC";

  public static Future<String> add(MongoClient mongo, String userId, JsonObject project) {
    JsonObject spec = project.getJsonObject(ProjectDB.SPEC);
    if (spec == null || spec.isEmpty()) {
      return Future.failedFuture("No spec found.");
    }

    String projectId = project.getString(ProjectDB.ID);
    JsonObject deploy = project.getJsonObject(ProjectDB.DEPLOY);
    JsonObject backend = deploy.getJsonObject(ProjectDB.DEPLOY_BACKEND);
    backend.remove(ProjectDB.DEPLOY_BACKEND_STATUS);

    JsonObject platform = deploy.getJsonObject(ProjectDB.DEPLOY_PLATFORM);
    if (platform != null) {
      platform = JsonKeyReplacer.compatBson(platform);
    }
    deploy.put(ProjectDB.DEPLOY_PLATFORM, platform);

    JsonObject exec = new JsonObject();
    exec.put(FIELD_USER_ID, userId)
        .put(FIELD_SPEC, spec)
        .put(FIELD_STATUS, ExecState.CREATE.toString())
        .put(FIELD_CREATE_TIME, System.currentTimeMillis())
        .put(FIELD_FROM_PROJECT, projectId)
        .put(FIELD_DEPLOY, deploy);

    return mongo.insert(DB, exec);
  }

  public static Future<JsonObject> updateStatus(MongoClient mongo, String execId,
      JsonObject execStatus) {
    ExecState status = ExecState.valueOf(execStatus.getString("status"));
    JsonObject update = new JsonObject();
    update.put(FIELD_STATUS, status.toString());
    switch (status) {
      case CREATE:
        update.put(FIELD_CREATE_TIME, execStatus.getLong("createTime"));
        break;
      case SUBMIT:
        update.put(FIELD_SUBMIT_TIME, execStatus.getLong("submitTime"));
        break;
      case RUNNING:
        update.put(FIELD_BEAT_TIME, execStatus.getLong("beatTime"));
        break;
      case FINISH:
        update.put(FIELD_FINISH_TIME, execStatus.getLong("finishTime"));
        break;
      case FAILURE:
        update.put(FIELD_TERMINATE_TIME, execStatus.getLong("terminateTime"));
        break;
    }

    return mongo.updateCollection(
        DB,
        new JsonObject().put(FIELD_ID, execId),
        new JsonObject().put("$set", update)
    ).compose(ret -> {
      return Future.<JsonObject>succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> updateByPath(MongoClient mongo, String execId,
      JsonObject updateByPath) {
    return mongo.updateCollection(
        DB,
        new JsonObject().put(FIELD_ID, execId),
        new JsonObject().put("$set", updateByPath)
    ).compose(ret -> {
      return Future.<JsonObject>succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> getOfId(MongoClient mongo, String execId) {
    return mongo.findOne(
        DB,
        new JsonObject()
            .put(FIELD_ID, execId),
        new JsonObject()
    ).compose((JsonObject exec) -> {
      if (exec == null || exec.isEmpty()) {
        return Future.succeededFuture(new JsonObject());
      }
      return Future.succeededFuture(compatJsonOnPlatform(exec));
    });
  }

  public static Future<JsonObject> getOfIdNoDetail(MongoClient mongo, String execId) {
    return getOfId(mongo, execId).compose((JsonObject exec) -> {
      if (exec == null || exec.isEmpty()) {
        return Future.succeededFuture(new JsonObject());
      }
      noDetail(exec);
      return Future.succeededFuture(exec);
    });
  }

  private static JsonObject noDetail(JsonObject exec) {
    JsonObject deploy = exec.getJsonObject(FIELD_DEPLOY);
    String deployId = deploy.getString(ProjectDB.DEPLOY_ID);
    int epoch = deploy.getInteger(ProjectDB.DEPLOY_EPOCH);
    exec.remove(FIELD_SPEC);
    exec.remove(FIELD_DEPLOY);
    exec.put("deployId", deployId);
    exec.put("epoch", epoch);
    return exec;
  }

  private static JsonObject compatJsonOnPlatform(JsonObject exec) {
    JsonObject platform = exec.getJsonObject(FIELD_DEPLOY).getJsonObject(ProjectDB.DEPLOY_PLATFORM);
    platform = JsonKeyReplacer.compatJson(platform);
    exec.getJsonObject(FIELD_DEPLOY).put(ProjectDB.DEPLOY_PLATFORM, platform);
    return exec;
  }

  public static Future<List<JsonObject>> getAllOfMatcher(MongoClient mongo, JsonObject matcher) {
    JsonObject match = new JsonObject();
    JsonObject lookup = new JsonObject();
    JsonObject project = new JsonObject();
    JsonObject reduceProject = new JsonObject();

    match.put("$match", matcher);
    lookup.put("$lookup",
        new JsonObject()
            .put("from", ProjectDB.DB)
            .put("localField", ExecDB.FIELD_FROM_PROJECT)
            .put("foreignField", ProjectDB.ID)
            .put("as", "fromProjectCopy")
    );

    project.put("$project",
        new JsonObject()
            .put(FIELD_ID, true)
            .put(FIELD_USER_ID, true)
            .put(FIELD_SPEC, true)
            .put(FIELD_DEPLOY, true)
            .put(FIELD_STATUS, true)
            .put(FIELD_CREATE_TIME, true)
            .put(FIELD_SUBMIT_TIME, true)
            .put(FIELD_BEAT_TIME, true)
            .put(FIELD_FINISH_TIME, true)
            .put(FIELD_TERMINATE_TIME, true)
            .put(FIELD_FROM_PROJECT, true)
            .put("fromProjectDetail", new JsonObject().put("$arrayElemAt",
                new JsonArray().add("$fromProjectCopy").add(0)))
    );

    reduceProject.put("$project",
        new JsonObject()
            .put(FIELD_ID, true)
            .put(FIELD_USER_ID, true)
            .put(FIELD_SPEC, true)
            .put(FIELD_DEPLOY, true)
            .put(FIELD_STATUS, true)
            .put(FIELD_CREATE_TIME, true)
            .put(FIELD_SUBMIT_TIME, true)
            .put(FIELD_BEAT_TIME, true)
            .put(FIELD_FINISH_TIME, true)
            .put(FIELD_TERMINATE_TIME, true)
            .put(FIELD_FROM_PROJECT, true)
            .put("fromProjectDetail", new JsonObject().put(ProjectDB.NAME, true))
    );

    JsonArray pipeline = new JsonArray()
        .add(match)
        .add(lookup)
        .add(project)
        .add(reduceProject);

    return ReadStreamCollector.<JsonObject, JsonObject>toList(
        mongo.aggregate(DB, pipeline), ExecDB::compatJsonOnPlatform
    );
  }

  public static Future<List<JsonObject>> getAllOfUser(MongoClient mongo, String userId) {
    JsonObject matcher = new JsonObject();
    matcher.put(FIELD_USER_ID, userId);
    return getAllOfMatcher(mongo, matcher);
  }

  public static Future<List<JsonObject>> getAllOfUserNoDetail(MongoClient mongo, String userId) {
    JsonObject matcher = new JsonObject();
    matcher.put(FIELD_USER_ID, userId);

    return ReadStreamCollector.<JsonObject, JsonObject>toList(
        mongo.findBatch(DB, matcher),
        ExecDB::noDetail
    );
  }

  public static Future<List<JsonObject>> getAll(MongoClient mongo) {
    JsonObject matcher = new JsonObject();

    return getAllOfMatcher(mongo, matcher);
  }

  public static Future<List<JsonObject>> getAllNoDetail(MongoClient mongo) {
    JsonObject matcher = new JsonObject();

    return ReadStreamCollector.<JsonObject, JsonObject>toList(
        mongo.findBatch(DB, matcher),
        ExecDB::noDetail
    );
  }

  public static Future<List<JsonObject>> getAllOfProject(MongoClient mongo, String projectId) {
    JsonObject matcher = new JsonObject();
    matcher.put(FIELD_FROM_PROJECT, projectId);

    return getAllOfMatcher(mongo, matcher);
  }

  public static Future<List<JsonObject>> getAllOfProjectNoDetail(MongoClient mongo,
      String projectId) {
    JsonObject matcher = new JsonObject();
    matcher.put(FIELD_FROM_PROJECT, projectId);

    return ReadStreamCollector.<JsonObject, JsonObject>toList(
        mongo.findBatch(DB, matcher),
        ExecDB::noDetail
    );
  }

  public static Future<JsonObject> forceRemove(MongoClient mongo, String userId, String execId) {
    return mongo.removeDocument(DB,
        new JsonObject().put(FIELD_USER_ID, userId).put(FIELD_ID, execId)).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> forceRemoveAllOfUser(MongoClient mongo, String userId) {
    return mongo.removeDocuments(DB, new JsonObject().put(FIELD_USER_ID, userId)).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> forceRemoveAllOfProject(MongoClient mongo, String userId,
      String projectId) {
    return mongo.removeDocuments(DB,
            new JsonObject().put(FIELD_USER_ID, userId).put(FIELD_FROM_PROJECT, projectId))
        .compose(ret -> {
          return Future.succeededFuture(ret.toJson());
        });
  }

  public static Future<JsonObject> forceRemoveAll(MongoClient mongo) {
    return mongo.removeDocuments(DB, new JsonObject()).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> remove(MongoClient mongo, String userId, String execId) {
    JsonObject matcher = terminateMatcher();
    matcher.put(FIELD_USER_ID, userId);
    matcher.put(FIELD_ID, execId);
    return mongo.removeDocument(DB, matcher).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  private static JsonObject terminateMatcher() {
    JsonObject matcher = new JsonObject();
    matcher.put(
        FIELD_STATUS,
        new JsonObject().put("$in",
            new JsonArray().add(ExecState.FINISH.toString()).add(ExecState.FAILURE.toString()))
    );
    return matcher;
  }

  public static Future<JsonObject> removeAllOfUser(MongoClient mongo, String userId) {
    JsonObject matcher = terminateMatcher();
    matcher.put(FIELD_USER_ID, userId);
    return mongo.removeDocument(DB, matcher).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> removeAllOfProject(MongoClient mongo, String userId,
      String projectId) {
    JsonObject matcher = terminateMatcher();
    matcher.put(FIELD_USER_ID, userId);
    matcher.put(FIELD_FROM_PROJECT, projectId);
    return mongo.removeDocument(DB, matcher).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }

  public static Future<JsonObject> removeAll(MongoClient mongo) {
    JsonObject matcher = terminateMatcher();
    return mongo.removeDocument(DB, matcher).compose(ret -> {
      return Future.succeededFuture(ret.toJson());
    });
  }
}
