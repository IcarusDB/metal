package org.metal.server.project;

import com.mongodb.DBRef;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.User;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.impl.MappingStream;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import org.metal.server.SendJson;

public class Project extends AbstractVerticle {
  private final static Logger LOGGER = LoggerFactory.getLogger(Project.class);

  private MongoClient mongo;

  private Project(MongoClient mongo) {
    this.mongo = mongo;
  }

  public static Project create(MongoClient mongo) {
    return new Project(mongo);
  }

  public void add(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();


    User user = ctx.user();
    mongo.findOne(
        "user",
        new JsonObject().put("username", user.get("username")),
        new JsonObject().put("_id", true)
    ).compose(
        (ret) -> {
          String id = ret.getString("_id");
          String backendId = UUID.randomUUID().toString();
          body.put("user", new JsonObject()
                  .put("$ref", "user")
                  .put("$id", id)
              )
              .put("createTime", System.currentTimeMillis())
              .put("backendId", backendId);
          return Future.succeededFuture(body.copy());
        }
    ).compose(
        (JsonObject ret) -> {
          return mongo.insert("projects", ret);
        }
    ).onSuccess(id -> {
      LOGGER.info("Project ID:" + id);
      JsonObject resp = new JsonObject();
      resp.put("status", "OK");
      SendJson.send(ctx, resp, 201);
    }).onFailure(error -> {
      LOGGER.error(error);
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", error.getLocalizedMessage());
      SendJson.send(ctx, resp, 500);
    });
  }

  public void getAll(RoutingContext ctx) {
    User user = ctx.user();
    if (user == null) {
      JsonObject resp = new JsonObject();
      resp.put("status", "FAIL")
          .put("msg", "No user has been authenicated.");
      SendJson.send(ctx, resp, 401);
      return;
    }

    JsonObject lookup = new JsonObject()
        .put("$lookup", new JsonObject()
            .put("from", "user")
            .put("localField", "user.$id")
            .put("foreignField", "_id")
            .put("as", "userInfo")
        );

    JsonObject match = new JsonObject()
        .put("$match", new JsonObject()
            .put("userInfo.username", user.get("username")));

    JsonObject project = new JsonObject()
        .put("$project",new JsonObject()
                .put("_id", true)
                .put("name", true)
                .put("createTime", true)
                .put("backendId", true)
                .put("userInfo", new JsonObject().put("username", true)));

    JsonArray pipeline = new JsonArray()
        .add(lookup)
        .add(match)
        .add(project);
    ReadStream<JsonObject> readStream = mongo.aggregate("projects", pipeline);
    ReadStream<Buffer> bufferReadStream = new MappingStream<JsonObject, Buffer>(readStream, (JsonObject json) -> {
      return json.toBuffer();
    });

    bufferReadStream.exceptionHandler(error -> {
      JsonObject resp = new JsonObject()
          .put("status", "FAIL")
          .put("msg", error.getLocalizedMessage());
      LOGGER.error(error);
      SendJson.send(ctx, resp, 500);
    });

    ctx.response().send(bufferReadStream)
        .onFailure(error -> {
          LOGGER.error(error);
        });
  }

  public void get(RoutingContext ctx) {
    String projectName = ctx.request().params().get("projectName");
    if (projectName == null || projectName.strip().isEmpty()) {
      JsonObject resp = new JsonObject();
      SendJson.send(ctx, resp, 404);
      return;
    }

    JsonObject match = new JsonObject()
        .put("$match", new JsonObject().put("name", projectName));

    JsonObject lookup = new JsonObject()
        .put("$lookup", new JsonObject()
            .put("from", "user")
            .put("localField", "user.$id")
            .put("foreignField", "_id")
            .put("as", "userInfo"));

    JsonObject project = new JsonObject()
        .put("$project", new JsonObject()
            .put("_id", true)
            .put("name", true)
            .put("createTime", true)
            .put("backendId", true)
            .put("userInfo", new JsonObject().put("username", true))
        );

    JsonArray pipeline = new JsonArray()
        .add(match)
        .add(lookup)
        .add(project);

    ReadStream<JsonObject> readStream = mongo.aggregate("projects", pipeline);
    ReadStream<Buffer> bufferReadStream = new MappingStream<JsonObject, Buffer>(
        readStream,
        (JsonObject json) -> {
          return json.toBuffer();
        }
    );

    bufferReadStream.exceptionHandler(error -> {
      JsonObject resp = new JsonObject()
          .put("status", "FAIL")
          .put("msg", error.getLocalizedMessage());
      SendJson.send(ctx, resp, 500);
    });

    ctx.response().send(bufferReadStream)
        .onFailure(error -> {
          LOGGER.error(error);
        });
  }

  public void setBackendLauncher(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Platform p= Platform.valueOf(body.getString("platform"));
    switch (p) {
      case SPARK: {

      }
    }
  }
}
