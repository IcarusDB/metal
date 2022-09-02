package org.metal.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class ServerLauncher {
  private final static Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    IServerProps props = ImmutableIServerProps.builder()
        .port(19000)
        .mongoConnection("mongodb://metal:123456@192.168.15.10:27017/metalDB")
        .init(false)
        .build();
    Server srv = new Server(props);

    vertx.deployVerticle(srv, deploymentOptions)
        .onSuccess(deployID -> {
          LOGGER.info(String.format("Success to deploy %s:%s.", srv.getClass(), deployID));
        })
        .onFailure(t -> {
          LOGGER.error(String.format("Fail to deploy %s.", srv.getClass()), t);
        });
  }

}
