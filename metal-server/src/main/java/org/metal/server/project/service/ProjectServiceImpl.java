package org.metal.server.project.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.Optional;
import org.metal.server.project.Platform;
import org.metal.server.project.ProjectDB;

public class ProjectServiceImpl implements IProjectService{
  private MongoClient mongo;
  private Vertx vertx;
  public ProjectServiceImpl(MongoClient mongo) {
    this.mongo = mongo;
  }

  public ProjectServiceImpl(Vertx vertx, MongoClient mongo) {
    this.vertx = vertx;
    this.mongo = mongo;
  }

  @Override
  public Future<String> createEmptyProject(String userId, String projectName) {
    return ProjectDB.add(mongo, userId, projectName);
  }

  @Override
  public Future<String> createProject(String userId, String projectName, String platform,
      JsonObject platformArgs, JsonObject backendArgs, JsonObject spec) {
    return ProjectDB.add(
        mongo,
        userId,
        projectName,
        Platform.valueOf(platform),
        platformArgs, backendArgs, spec
    );
  }

  @Override
  public Future<String> createProjectFrom(String userId, String projectName) {
    return ProjectDB.copyFrom(mongo, userId, projectName);
  }

  @Override
  public Future<String> createProjectFromExec(String userId, String execId) {
    return ProjectDB.recoverFrom(mongo, userId, execId);
  }

  @Override
  public Future<JsonObject> updateName(String userId, String projectName, String newProjectName) {
    return ProjectDB.updateProjectName(mongo, userId, projectName, newProjectName);
  }



  @Override
  public Future<JsonObject> updateSpec(String userId, String projectName, JsonObject spec) {
    return ProjectDB.update(
        mongo,
        userId,
        projectName,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.of(spec)
    );
  }

  @Override
  public Future<JsonObject> updatePlatform(String userId, String projectName, String platform,
      JsonObject platformArgs, JsonObject backendArgs) {
    return ProjectDB.update(
        mongo,
        userId,
        projectName,
        Optional.of(Platform.valueOf(platform)),
        Optional.of(platformArgs),
        Optional.of(backendArgs),
        Optional.empty()
    );
  }

  @Override
  public Future<JsonObject> updateByPath(String userId, String projectName, JsonObject updateByPath) {
    return ProjectDB.updateByPath(mongo, userId, projectName, updateByPath);
  }

  @Override
  public Future<JsonObject> getOfId(String userId, String projectId) {
    return ProjectDB.getOfId(mongo, userId, projectId);
  }

  @Override
  public Future<JsonObject> getOfName(String userId, String projectName) {
    System.out.println(vertx instanceof VertxInternal);
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.getOfName(mongo, userId, projectName)
    ).compose((List<JsonObject> projects) -> {
      if (projects.isEmpty()) {
        return Future.succeededFuture(new JsonObject());
      } else {
        return Future.succeededFuture(projects.get(0));
      }
    });
  }

  @Override
  public Future<List<JsonObject>> getAllOfUser(String userId) {
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.getAllOfUser(mongo, userId)
    );
  }

  @Override
  public Future<List<JsonObject>> getAll() {
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.getAll(mongo)
    );
  }

  @Override
  public Future<Long> removeOfId(String userId, String projectId) {
    return ProjectDB.removeOfId(mongo, userId, projectId);
  }

  @Override
  public Future<Long> removeOfName(String userId, String projectName) {
    return ProjectDB.removeOfName(mongo, userId, projectName);
  }

  @Override
  public Future<Long> removeAllOfUser(String userId) {
    return ProjectDB.removeAllOfUser(mongo, userId);
  }

  @Override
  public Future<Long> removeAll() {
    return ProjectDB.removeAll(mongo);
  }
}
