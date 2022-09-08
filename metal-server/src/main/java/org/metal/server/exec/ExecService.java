package org.metal.server.exec;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import java.util.List;

@ProxyGen
@VertxGen
public interface ExecService {
    public Future<String> add(JsonObject exec);

    public Future<Void> remove(String execId);

    public Future<Void> update(String execId, JsonObject exec);

    public Future<JsonObject> get(String execId);

    public Future<List<JsonObject>> getAll();

    public Future<List<JsonObject>> getAllOfUser(String username);
}
