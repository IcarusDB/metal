package org.metal.server.exec;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import org.metal.server.api.ExecState;
import org.metal.server.project.ProjectDB;
import org.metal.server.project.service.ProjectDBEx;

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

  public final static String FIELD_ARGS = "args";

  public final static String FIELD_DEPLOY = "deploy";
  public final static String FIELD_FROM_PROJECT = "fromProject";
  public final static String FIELD_SPEC = "SPEC";

  public static Future<String> add(MongoClient mongo, String userId, JsonObject project, JsonObject execArgs) {
    JsonObject spec = project.getJsonObject(ProjectDBEx.SPEC);
    if (spec == null || spec.isEmpty()) {
      return Future.failedFuture("No spec found.");
    }

    String projectId = project.getString(ProjectDBEx.ID);
    JsonObject deploy = project.getJsonObject(ProjectDBEx.DEPLOY);
    JsonObject backend = deploy.getJsonObject(ProjectDBEx.DEPLOY_BACKEND);
    backend.remove(ProjectDBEx.DEPLOY_BACKEND_STATUS);
    JsonObject exec = new JsonObject();
    exec.put(FIELD_USER_ID, userId)
        .put(FIELD_SPEC, spec)
        .put(FIELD_STATUS, ExecState.CREATE.toString())
        .put(FIELD_CREATE_TIME, System.currentTimeMillis())
        .put(FIELD_ARGS, execArgs)
        .put(FIELD_FROM_PROJECT, projectId)
        .put(FIELD_DEPLOY, deploy);

    return mongo.insert(DB, exec);
  }

  public static Future<JsonObject> updateStatus(MongoClient mongo, String execId, ExecState status) {
    JsonObject update = new JsonObject()
        .put(FIELD_STATUS, status.toString());
    switch (status) {
      case CREATE: update.put(FIELD_CREATE_TIME, System.currentTimeMillis()); break;
      case SUBMIT: update.put(FIELD_SUBMIT_TIME, System.currentTimeMillis()); break;
      case RUNNING: update.put(FIELD_BEAT_TIME, System.currentTimeMillis()); break;
      case FINISH: update.put(FIELD_FINISH_TIME, System.currentTimeMillis()); break;
      case FAILURE: update.put(FIELD_TERMINATE_TIME, System.currentTimeMillis()); break;
    }

    return mongo.findOneAndUpdate(
        DB,
        new JsonObject().put(FIELD_ID , execId),
        new JsonObject().put("$set", update)
    );
  }

  public static Future<JsonObject> updateStatus(MongoClient mongo, JsonObject execStatus) {
    String execId = execStatus.getString("id");
    ExecState status = ExecState.valueOf(execStatus.getString("status"));
    JsonObject update = new JsonObject();
    update.put(FIELD_STATUS, status.toString());
    switch (status) {
      case CREATE: update.put(FIELD_CREATE_TIME, execStatus.getLong("createTime")); break;
      case SUBMIT: update.put(FIELD_SUBMIT_TIME, execStatus.getLong("submitTime")); break;
      case RUNNING: update.put(FIELD_BEAT_TIME, execStatus.getLong("beatTime")); break;
      case FINISH: update.put(FIELD_FINISH_TIME, execStatus.getLong("finishTime")); break;
      case FAILURE: update.put(FIELD_TERMINATE_TIME, execStatus.getLong("terminateTime")); break;
    }

    return mongo.updateCollection(
        DB,
        new JsonObject().put(FIELD_ID, execId),
        new JsonObject().put("$set", update)
    ).compose(ret -> {return Future.<JsonObject>succeededFuture(ret.toJson());});
  }

  public static Future<JsonObject> updateByPath(MongoClient mongo, String execId, JsonObject updateByPath) {
    return mongo.updateCollection(
        DB,
        new JsonObject().put(FIELD_ID, execId),
        new JsonObject().put("$set", updateByPath)
    ).compose(ret -> {return Future.<JsonObject>succeededFuture(ret.toJson());});
  }

  public static Future<JsonObject> get(MongoClient mongo, String execId) {
    return mongo.findOne(
        DB,
        new JsonObject()
            .put(FIELD_ID , execId),
        new JsonObject()
    );
  }

  public static ReadStream<JsonObject> getAllOfUser(MongoClient mongo, String userId) {
    return mongo.findBatch(
        DB,
        new JsonObject().put(FIELD_USER_ID, userId)
    );
  }

  public static ReadStream<JsonObject> getAll(MongoClient mongo) {
    return mongo.findBatch(
        DB,
        new JsonObject()
    );
  }

  public static Future<MongoClientDeleteResult> remove(MongoClient mongo, String userId, String execId) {
    return mongo.removeDocument(DB, new JsonObject().put(FIELD_USER_ID, userId).put(FIELD_ID , execId));
  }

  public static Future<MongoClientDeleteResult> removeAllOfUser(MongoClient mongo, String userId) {
    return mongo.removeDocuments(DB, new JsonObject().put(FIELD_USER_ID, userId));
  }

  public static Future<MongoClientDeleteResult> removeAll(MongoClient mongo) {
    return mongo.removeDocuments(DB, new JsonObject());
  }
}
