package org.metal.server;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.metal.server.project.Project;

public class ServerLauncher {
  private final static Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

  public static void main(String[] args) {
    JsonObject zkConfig = new JsonObject();
    zkConfig.put("zookeeperHosts", "192.168.8.201")
        .put("sessionTimeout", 20000)
        .put("connectionTimeout", 3000)
        .put("rootPath", "io.vertx")
        .put("retry", new JsonObject()
            .put("initialSleepTime", 100)
            .put("intervalTimes", 10000)
            .put("maxTimes", 5));

    ClusterManager clusterManager = new ZookeeperClusterManager(zkConfig);
    VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    deploymentOptions.setConfig(
        new JsonObject()
            .put("mongoConnection", "mongodb://metal:123456@192.168.15.10:27017/metalDB")
            .put("projectAddress", "project.metal.org")
            .put("gatewayPort", 19000)
    );

    Vertx.clusteredVertx(options).compose((Vertx vertx) -> {
      Project project = Project.create();
      Server srv = Server.create();
      vertx.exceptionHandler(t -> {
        LOGGER.error(t);
      });

      return vertx.deployVerticle(project, deploymentOptions)
          .compose(
              deployID -> {
            LOGGER.info(String.format("Success to deploy %s:%s.", project.getClass(), deployID));
            return vertx.deployVerticle(srv, deploymentOptions);
          }, t -> {
            LOGGER.error(String.format("Fail to deploy %s.", project.getClass()), t);
            return Future.failedFuture(t);
          }).compose(deployID -> {
            LOGGER.info(String.format("Success to deploy %s:%s.", srv.getClass(), deployID));
            return Future.succeededFuture();
          }, t -> {
            LOGGER.error(String.format("Fail to deploy %s.", srv.getClass()), t);
            return Future.failedFuture(t);
          });
    }).onSuccess(ret -> {
      LOGGER.info("Success to deploy");
    }).onFailure(t -> {
      LOGGER.error("FAIL to deploy");
    });
  }

}
