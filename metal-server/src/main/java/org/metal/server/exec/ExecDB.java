package org.metal.server.exec;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import java.util.Optional;
import org.metal.server.project.ProjectDB;

public class ExecDB {
  public static enum State {
    CREATE, SUBMIT, RUNNING, FINISH, FAIL
  }

  public final static String DB = "execs";
  public final static String FIELD_ID = "_id";
  public final static String FIELD_USER_ID = "userId";
  public final static String FIELD_CREATE_TIME = "createTime";
  public final static String FIELD_SUBMIT_TIME = "submitTime";
  public final static String FIELD_BEAT_TIME = "beatTime";
  public final static String FIELD_FINISH_TIME = "finishTime";
  public final static String FIELD_TERMINATE_TIME = "terminateTime";
  public final static String FIELD_STATUS = "status";
  public final static String FIELD_DEPLOY_ID = "deployId";
  public final static String FIELD_DEPLOY_ARGS = "deployArgs";
  public final static String FIELD_DEPLOY_ARGS_PLATFORM = "platform";
  public final static String FIELD_DEPLOY_ARGS_PLATFORM_ARGS = "platformArgs";
  public final static String FIELD_DEPLOY_ARGS_BACKEND_ARGS = "backendArgs";
  public final static String FIELD_FROM_PROJECT = "fromProject";
  public final static String FIELD_SPEC = "SPEC";

  public static Future<String> add(MongoClient mongo, String userId, String projectId) {
    return ProjectDB.getOfId(mongo, userId, projectId)
        .compose((JsonObject project) -> {
          JsonObject exec = new JsonObject();
          exec.put(FIELD_FROM_PROJECT, projectId)
              .put(FIELD_USER_ID, userId)
              .put(FIELD_CREATE_TIME, System.currentTimeMillis())
              .put(FIELD_STATUS, State.CREATE)
              .put(FIELD_DEPLOY_ID, project.getString(ProjectDB.FIELD_DEPLOY_ID))
              .put(FIELD_DEPLOY_ARGS, project.getJsonObject(ProjectDB.FIELD_DEPLOY_ARGS))
              .put(FIELD_SPEC, project.getJsonObject(ProjectDB.FIELD_SPEC));
          return mongo.insert(DB, exec);
        });
  }

  public static Future<JsonObject> updateStatus(MongoClient mongo, String userId, String execId, State status) {
    JsonObject update = new JsonObject()
        .put(FIELD_STATUS, status);
    switch (status) {
      case CREATE: update.put(FIELD_CREATE_TIME, System.currentTimeMillis()); break;
      case SUBMIT: update.put(FIELD_SUBMIT_TIME, System.currentTimeMillis()); break;
      case RUNNING: update.put(FIELD_BEAT_TIME, System.currentTimeMillis()); break;
      case FINISH: update.put(FIELD_FINISH_TIME, System.currentTimeMillis()); break;
      case FAIL: update.put(FIELD_TERMINATE_TIME, System.currentTimeMillis()); break;
    }

    return mongo.findOneAndUpdate(
        DB,
        new JsonObject().put(FIELD_USER_ID, userId).put(FIELD_ID , execId),
        update
    );
  }

  public static Future<JsonObject> get(MongoClient mongo, String userId, String execId) {
    return mongo.findOne(
        DB,
        new JsonObject().put(FIELD_USER_ID, userId)
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
