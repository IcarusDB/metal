package org.metal.server.project;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import java.util.Optional;
import java.util.UUID;
import org.metal.server.exec.ExecDB;

public class ProjectDB {
  public final static String DB = "projects";

  public static Future<Void> createCollection(MongoClient mongo) {
    return mongo.createCollection(DB)
        .compose(ret -> {
          return mongo.createIndexWithOptions(
              DB,
              new JsonObject()
                  .put("user", new JsonObject().put("$id", true))
                  .put("name", true),
              new IndexOptions().unique(true)
          );
        });
  }

  public static Future<String> add(
      MongoClient mongo,
      String userId,
      String projectName,
      Platform platform, JsonObject platformArgs, JsonObject backendArgs,
      JsonObject spec
      ) {
    JsonObject project = new JsonObject();
    project.put("user", new JsonObject()
        .put("$ref", "user")
        .put("$id", userId));

    JsonObject deployArgs = new JsonObject()
        .put("platform", platform)
        .put("platformArgs", platformArgs)
        .put("backendArgs", backendArgs);

    project.put("createTime", System.currentTimeMillis())
        .put("deployId", UUID.randomUUID().toString())
        .put("deployArgs", deployArgs)
        .put("name", projectName)
        .put("spec", spec);

    return mongo.insert(DB, project);
  }

  public static Future<String> copyFrom(MongoClient mongo, String userId, String projectName) {
    return mongo.findOne(
        DB,
        new JsonObject()
            .put("name", projectName)
            .put("user", new JsonObject().put("$id", userId)),
        new JsonObject()
        ).compose((JsonObject project) -> {
          project.put("deployId", UUID.randomUUID().toString())
              .put("createTime", System.currentTimeMillis())
              .put("name", projectName + "_copy_" + UUID.randomUUID().toString().substring(0, 4));
          return mongo.insert(DB, project);
    });
  }

  public static Future<String> recoverFrom(MongoClient mongo, String userId, String execId) {
    return ExecDB.get(mongo, userId, execId)
        .compose((JsonObject exec) -> {
          JsonObject project = new JsonObject();
          JsonObject deployArgs = exec.getJsonObject("deployArgs");
          return add(mongo,
              userId,
              "recover_" + execId,
              Platform.valueOf(exec.getString("platform")),
              deployArgs.getJsonObject("platformArgs"),
              deployArgs.getJsonObject("backendArgs"),
              exec.getJsonObject("spec")
              );
        });
  }

  private static JsonObject emptySpec() {
    return new JsonObject()
        .put("version", "1.0")
        .put("metals", new JsonArray())
        .put("edges", new JsonArray());
  }

  public static Future<String> add( MongoClient mongo, String userId, String projectName) {
    return ProjectDB.add(mongo, userId, projectName,
        Platform.SPARK, new JsonObject(), new JsonObject(),
        emptySpec());
  }

  public static Future<String> add( MongoClient mongo, String userId, String projectName, Platform platform) {
    return ProjectDB.add(mongo, userId, projectName,
        platform, new JsonObject(), new JsonObject(),
        emptySpec());
  }

  public static Future<JsonObject> update(
      MongoClient mongo,
      String userId,
      String projectName,
      Optional<Platform> platform,
      Optional<JsonObject> platformArgs,
      Optional<JsonObject> backendArgs,
      Optional<JsonObject> spec
      ) {
    JsonObject update = new JsonObject();
    platform.ifPresent(p -> {
      update.put("platform", p.toString());
    });

    platformArgs.ifPresent(args -> {
      update.put("platformArgs", args);
    });

    backendArgs.ifPresent(args -> {
      update.put("backendArgs", args);
    });

    if (platform.isPresent() || platformArgs.isPresent() || backendArgs.isPresent()) {
      update.put("deployId", UUID.randomUUID().toString());
    }

    spec.ifPresent(s -> {
      update.put("spec", s);
    });

    return mongo.findOneAndUpdate(
        DB,
        new JsonObject().put("name", projectName).put("user", new JsonObject().put("$id", userId)),
        update
        );
  }

  public static Future<JsonObject> updateProjectName(MongoClient mongo, String userId, String projectName, String newProjectName) {
    return mongo.findOneAndUpdate(
      DB,
      new JsonObject()
          .put("user", new JsonObject().put("$id", userId))
          .put("name", projectName),
      new JsonObject()
            .put("name", newProjectName)
    );
  }

  public static Future<JsonObject> get(MongoClient mongo, String userId, String projectId, Optional<String> projectName) {
    return mongo.findOne(
        DB,
        new JsonObject()
            .put("_id", projectId)
            .put("user", new JsonObject().put("$id", userId)),
        new JsonObject()
    );
  }

  public static ReadStream<JsonObject> get(MongoClient mongo, String userId, String projectName) {
    JsonObject matchProjectName = new JsonObject()
        .put("$match", new JsonObject().put("name", projectName));

    JsonObject lookup = new JsonObject()
        .put("$lookup", new JsonObject()
            .put("from", "user")
            .put("localField", "user.$id")
            .put("foreignField", "_id")
            .put("as", "userInfo"));

    JsonObject project = new JsonObject()
        .put("$project",  new JsonObject()
            .put("_id", true)
            .put("name", true)
            .put("createTime", true)
            .put("deployId", true)
            .put("deployArgs", true)
            .put("userInfo", new JsonObject().put("$arrayElemAt", new JsonArray().add("$userInfo").add(0))));

    JsonObject matchUserId = new JsonObject()
        .put("$match", new JsonObject().put("userInfo._id", userId));

    JsonObject projectPWD = new JsonObject()
        .put("$project", new JsonObject()
            .put("userInfo", new JsonObject().put("password", false))
        );


    JsonArray pipeline = new JsonArray()
        .add(matchProjectName)
        .add(lookup)
        .add(project)
        .add(matchUserId)
        .add(projectPWD);
    System.out.println(pipeline.toString());
    return mongo.aggregate(DB, pipeline);
  }

  public static ReadStream<JsonObject> getAllOfUser(MongoClient mongo, String userId) {
    JsonObject matchUserId = new JsonObject()
        .put("$match", new JsonObject().put("user", new JsonObject().put("$id", userId)));

    JsonObject lookup = new JsonObject()
        .put("$lookup", new JsonObject()
            .put("from", "user")
            .put("localField", "user.$id")
            .put("foreignField", "_id")
            .put("as", "userInfo"));

    JsonObject project = new JsonObject()
        .put("$project",  new JsonObject()
            .put("_id", true)
            .put("name", true)
            .put("createTime", true)
            .put("deployId", true)
            .put("deployArgs", true)
            .put("userInfo", new JsonObject().put("username", true)));

    JsonArray pipeline = new JsonArray()
        .add(matchUserId)
        .add(lookup)
        .add(project);

    return mongo.aggregate(DB, pipeline);
  }

  public static ReadStream<JsonObject> getAll(MongoClient mongo) {
    JsonObject lookup = new JsonObject()
        .put("$lookup", new JsonObject()
            .put("from", "user")
            .put("localField", "user.$id")
            .put("foreignField", "_id")
            .put("as", "userInfo"));

    JsonObject project = new JsonObject()
        .put("$project",  new JsonObject()
            .put("_id", true)
            .put("name", true)
            .put("createTime", true)
            .put("deployId", true)
            .put("deployArgs", true)
            .put("userInfo", new JsonObject().put("username", true)));

    JsonArray pipeline = new JsonArray()
        .add(lookup)
        .add(project);

    return mongo.aggregate(DB, pipeline);
  }

  public static Future<MongoClientDeleteResult> remove(MongoClient mongo, String userId, String projectName) {
    return mongo.removeDocument(
        DB,
        new JsonObject()
            .put("name", projectName)
            .put("user", new JsonObject().put("$id", userId))
    );
  }

  public static Future<MongoClientDeleteResult> removeAllOfUser(MongoClient mongo, String userId) {
    return mongo.removeDocuments(
        DB,
        new JsonObject()
            .put("user", new JsonObject().put("$id", userId))
    );
  }

  public static Future<MongoClientDeleteResult> removeAll(MongoClient mongo) {
    return mongo.removeDocuments(DB, new JsonObject());
  }

}
