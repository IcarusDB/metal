package org.metal.server.exec.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import org.metal.server.exec.ExecService;

public class ExecServiceImpl implements ExecService {
  private MongoClient mongo;

  @Override
  public Future<String> add(JsonObject exec) {
    if (exec.getString("id") == null) {

    }

    if (exec.getString("deployId") == null) {

    }

    if (exec.getString("specId") == null) {

    }

    exec.put("status", "UNSUBMIT");
    exec.put("createTime", System.currentTimeMillis());

    return mongo.insert("exec", exec);
  }

  @Override
  public Future<Void> remove(String execId) {
    return null;
  }

  @Override
  public Future<Void> update(String execId, JsonObject exec) {
    return null;
  }

  @Override
  public Future<JsonObject> get(String execId) {
    return null;
  }

  @Override
  public Future<List<JsonObject>> getAll() {
    return null;
  }

  @Override
  public Future<List<JsonObject>> getAllOfUser(String username) {
    return null;
  }
}
