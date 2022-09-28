package org.metal.server.exec;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;
import org.metal.server.exec.impl.ExecServiceImpl;

@ProxyGen
@VertxGen
public interface ExecService {
    public static ExecService create(Vertx vertx, JsonObject conf) {
        String address = conf.getString("address");
        return new ExecServiceVertxEBProxy(vertx, address);
    }

    public static ExecService createProvider(Vertx vertx, MongoClient mongo, JsonObject conf) {
        return new ExecServiceImpl(vertx, mongo, conf);
    }

    public Future<String> add(String userId, JsonObject exec);

    public Future<Void> remove(String execId);

    public Future<Void> update(String execId, JsonObject update);

    public Future<Void> updateStatus(String execId, JsonObject execStatus);

    public Future<JsonObject> getStatus(String execId);

    public Future<JsonObject> get(String execId);

    public Future<List<JsonObject>> getAll();

    public Future<List<JsonObject>> getAllOfUser(String userId);

    public Future<List<JsonObject>> getAllOfProject(String userId, String projectId);
}
