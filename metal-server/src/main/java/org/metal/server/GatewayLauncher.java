package org.metal.server;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import java.util.List;
import org.metal.server.project.Project;

public class GatewayLauncher {
  private final static Logger LOGGER = LoggerFactory.getLogger(GatewayLauncher.class);

  public static void main(String[] args) {
    ClusterManager clusterManager = new ZookeeperClusterManager("zookeeper.json");
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
      Gateway gateway = Gateway.create();
      vertx.exceptionHandler(t -> {
        LOGGER.error(t);
      });

      Future<String> deployProject = vertx.deployVerticle(project, deploymentOptions);
      deployProject = deployProject.compose(
          deployID -> {
            LOGGER.info(String.format("Success to deploy %s:%s.", project.getClass(), deployID));
            return Future.succeededFuture();
          }, t -> {
            LOGGER.error(String.format("Fail to deploy %s.", project.getClass()), t);
            return Future.failedFuture(t);
          });

      CompositeFuture prepared = CompositeFuture.all(List.of(deployProject));
      Future<String> deployGateway = prepared.compose(
          ret -> {
            return vertx.deployVerticle(gateway, deploymentOptions);
          }
      );

      deployGateway = deployGateway.compose(deployID -> {
            LOGGER.info(String.format("Success to deploy %s:%s.", gateway.getClass(), deployID));
            return Future.succeededFuture();
          }, t -> {
            LOGGER.error(String.format("Fail to deploy %s.", gateway.getClass()), t);
            return Future.failedFuture(t);
          });
      return deployGateway;
    }).onSuccess(ret -> {
      LOGGER.info("Success to deploy");
    }).onFailure(t -> {
      LOGGER.error("FAIL to deploy");
    });
  }

}
