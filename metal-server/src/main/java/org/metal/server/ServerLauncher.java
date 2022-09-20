package org.metal.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.metal.server.project.Project;

public class ServerLauncher {
  private final static Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    deploymentOptions.setConfig(
        new JsonObject()
            .put("mongoConnection", "mongodb://metal:123456@192.168.15.10:27017/metalDB")
            .put("projectAddress", "project.metal.org")
    );

    IServerProps props = ImmutableIServerProps.builder()
        .port(19000)
        .mongoConnection("mongodb://metal:123456@192.168.15.10:27017/metalDB")
        .init(false)
        .build();

    Project project = Project.create();
    Server srv = new Server(props);

    vertx.deployVerticle(project, deploymentOptions)
        .compose(deployID -> {
          LOGGER.info(String.format("Success to deploy %s:%s.", project.getClass(), deployID));
          return vertx.deployVerticle(srv, deploymentOptions);
        }, t -> {
          LOGGER.error(String.format("Fail to deploy %s.", project.getClass()), t);
          return Future.failedFuture(t);
        }).onSuccess(deployID -> {
          LOGGER.info(String.format("Success to deploy %s:%s.", srv.getClass(), deployID));
        })
        .onFailure(t -> {
          LOGGER.error(String.format("Fail to deploy %s.", srv.getClass()), t);
        });
  }

}
