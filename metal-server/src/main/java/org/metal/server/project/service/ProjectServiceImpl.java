package org.metal.server.project.service;

import io.vertx.core.Future;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import java.util.Optional;
import org.metal.server.project.Platform;
import org.metal.server.project.ProjectDB;

public class ProjectServiceImpl implements IProjectService{
  private MongoClient mongo;
  private VertxInternal vertxInternal;

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
  public Future<JsonObject> getOfId(String userId, String projectId) {
    return ProjectDB.get(mongo, userId, projectId, Optional.<String>empty());
  }

  @Override
  public Future<List<JsonObject>> getOfName(String userId, String projectName) {
    return ReadStreamCollector.<JsonObject>toList(
        ProjectDB.get(mongo, userId, projectName)
    );
  }

  @Override
  public Future<List<JsonObject>> getAllOfUser(String userId) {
    return null;
  }

  @Override
  public Future<List<JsonObject>> getAll() {
    return null;
  }

  @Override
  public Future<String> removeOfId(String userId, String projectId) {
    return null;
  }

  @Override
  public Future<String> removeOfName(String userId, String projectName) {
    return null;
  }

  @Override
  public Future<String> removeAllOfUser(String userId) {
    return null;
  }

  @Override
  public Future<String> removeAll() {
    return null;
  }
}
