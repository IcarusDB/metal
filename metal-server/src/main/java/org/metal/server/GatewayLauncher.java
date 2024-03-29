/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.server;

import org.metal.server.detector.Detector;
import org.metal.server.exec.Exec;
import org.metal.server.project.Project;
import org.metal.server.repo.MetalRepo;
import org.metal.server.report.BackendReport;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

import java.util.List;

public class GatewayLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayLauncher.class);

    public static void main(String[] args) {
        ClusterManager clusterManager = new ZookeeperClusterManager("zookeeper.json");
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
        DeploymentOptions deploymentOptions = new DeploymentOptions();

        Vertx.clusteredVertx(options)
                .compose(
                        (Vertx vertx) -> {
                            MetalRepo metalRepo = MetalRepo.create();
                            Exec exec = Exec.create();
                            Project project = Project.create();
                            Detector detector = Detector.create();
                            BackendReport backendReport = BackendReport.create();
                            Gateway gateway = Gateway.create();

                            vertx.exceptionHandler(
                                    t -> {
                                        LOGGER.error(t);
                                    });

                            Future<String> deployMetalRepo =
                                    vertx.deployVerticle(metalRepo, deploymentOptions)
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        metalRepo.getClass(),
                                                                        deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        metalRepo.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });

                            Future<String> deployProject =
                                    vertx.deployVerticle(project, deploymentOptions)
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        project.getClass(),
                                                                        deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        project.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });

                            Future<String> deployDetector =
                                    vertx.deployVerticle(detector, deploymentOptions)
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        detector.getClass(),
                                                                        deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        detector.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });

                            Future<String> deployBackendReport =
                                    vertx.deployVerticle(backendReport, deploymentOptions)
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        backendReport.getClass(),
                                                                        deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        backendReport.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });

                            Future<String> deployExec =
                                    vertx.deployVerticle(exec, deploymentOptions)
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        exec.getClass(), deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        exec.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });

                            CompositeFuture prepared =
                                    CompositeFuture.all(
                                            List.of(
                                                    deployMetalRepo,
                                                    deployProject,
                                                    deployDetector,
                                                    deployBackendReport,
                                                    deployExec));
                            Future<String> deployGateway =
                                    prepared.compose(
                                                    ret -> {
                                                        return vertx.deployVerticle(
                                                                gateway, deploymentOptions);
                                                    })
                                            .compose(
                                                    deployID -> {
                                                        LOGGER.info(
                                                                String.format(
                                                                        "Success to deploy %s:%s.",
                                                                        gateway.getClass(),
                                                                        deployID));
                                                        return Future.succeededFuture();
                                                    },
                                                    t -> {
                                                        LOGGER.error(
                                                                String.format(
                                                                        "Fail to deploy %s.",
                                                                        gateway.getClass()),
                                                                t);
                                                        return Future.failedFuture(t);
                                                    });
                            return Future.succeededFuture();
                        })
                .onSuccess(
                        ret -> {
                            LOGGER.info("Success to deploy");
                        })
                .onFailure(
                        t -> {
                            LOGGER.error("FAIL to deploy");
                        });
    }
}
