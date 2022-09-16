package org.metal.server.exec;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
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

  public static Future<String> add(MongoClient mongo, String userId, String projectId) {
    return ProjectDB.get(mongo, userId, projectId, Optional.<String>empty())
        .compose((JsonObject project) -> {
          JsonObject exec = new JsonObject();
          exec.put("fromProject", projectId)
              .put("userId", userId)
              .put("createTime", System.currentTimeMillis())
              .put("status", State.CREATE)
              .put("deployId", project.getString("deployId"))
              .put("deployArgs", project.getJsonObject("deployArgs"))
              .put("spec", project.getJsonObject("spec"));
          return mongo.insert(DB, exec);
        });
  }

  public static Future<JsonObject> updateStatus(MongoClient mongo, String userId, String execId, State status) {
    JsonObject update = new JsonObject()
        .put("status", status);
    switch (status) {
      case CREATE: update.put("createTime", System.currentTimeMillis()); break;
      case SUBMIT: update.put("submitTime", System.currentTimeMillis()); break;
      case RUNNING: update.put("heartbeatTime", System.currentTimeMillis()); break;
      case FINISH: update.put("finishTime", System.currentTimeMillis()); break;
      case FAIL: update.put("terminateTime", System.currentTimeMillis()); break;
    }

    return mongo.findOneAndUpdate(
        DB,
        new JsonObject().put("userId", userId).put("_id", execId),
        update
    );
  }

  public static Future<JsonObject> get(MongoClient mongo, String userId, String execId) {
    return mongo.findOne(
        DB,
        new JsonObject().put("userId", userId)
            .put("_id", execId),
        new JsonObject()
    );
  }

  public static ReadStream<JsonObject> getAllOfUser(MongoClient mongo, String userId) {
    return mongo.findBatch(
        DB,
        new JsonObject().put("userId", userId)
    );
  }

  public static ReadStream<JsonObject> getAll(MongoClient mongo) {
    return mongo.findBatch(
        DB,
        new JsonObject()
    );
  }

  public static Future<MongoClientDeleteResult> remove(MongoClient mongo, String userId, String execId) {
    return mongo.removeDocument(DB, new JsonObject().put("userId", userId).put("_id", execId));
  }

  public static Future<MongoClientDeleteResult> removeAllOfUser(MongoClient mongo, String userId) {
    return mongo.removeDocuments(DB, new JsonObject().put("userId", userId));
  }

  public static Future<MongoClientDeleteResult> removeAll(MongoClient mongo) {
    return mongo.removeDocuments(DB, new JsonObject());
  }
}
