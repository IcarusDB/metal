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

package org.metal.backend;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.metal.draft.DraftMaster;
import org.metal.service.BaseMetalService;
import org.metal.specs.Spec;

public class BackendLauncher {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackendLauncher.class);

  public static void main(String[] args) throws IOException, ParseException {
    Options options = BackendCli.create();
    CommandLine cli = BackendCli.parser(args, options);
    BackendDeployOptions deployOptions = BackendCli.parseDeployOptions(cli);
    if (cli.hasOption(BackendCli.HELP_OPT)) {
      BackendCli.printHelp();
    } else {
      tryRunCMD(cli, deployOptions);
      tryRunInteractive(cli, deployOptions);
    }
  }

  private static void tryRunCMD(CommandLine cli, BackendDeployOptions deployOptions) {
    if (!BackendCli.isCmdMode(cli)) {
      return;
    }

    Optional<Spec> optionalSpec = BackendCli.tryCmdMode(cli);
    if (optionalSpec.isPresent()) {
      IBackend.IBuilder builder = BackendManager.getBackendBuilder().get();
      builder.deployOptions(deployOptions);
      IBackend backend = builder.build();
      backend.start();
      BaseMetalService service = backend.service();
      service.analyse(DraftMaster.draft(optionalSpec.get()));
      service.exec();
      backend.stop();
    }
  }

  private static void tryRunInteractive(CommandLine cli, BackendDeployOptions deployOptions) {
    if (!BackendCli.isInteractiveMode(cli)) {
      return;
    }

    Optional<String> deployId = BackendCli.parseDeployId(cli);
    Optional<Integer> deployEpoch = BackendCli.parseDeployEpoch(cli);
    Optional<String> reportServiceAddress = BackendCli.parseReportServiceAddress(cli);
    Optional<Integer> restApiPort = BackendCli.parseRestApiPort(cli);

    if (deployId.isEmpty() || deployId.get().isBlank()) {
      String msg = String.format("%s is not set.", BackendCli.DEPLOY_ID_OPT.getLongOpt());
      LOGGER.error(msg);
      return;
    }

    if (deployEpoch.isEmpty()) {
      String msg = String.format("%s is not set.", BackendCli.DEPLOY_EPOCH_OPT.getLongOpt());
      LOGGER.error(msg);
      return;
    }

    if (reportServiceAddress.isEmpty() || reportServiceAddress.get().isBlank()) {
      String msg = String.format("%s is not set.",
          BackendCli.REPORT_SERVICE_ADDRESS_OPT.getLongOpt());
      LOGGER.error(msg);
      return;
    }

    if (restApiPort.isEmpty()) {
      String msg = String.format("%s is not set.", BackendCli.REST_API_PORT_OPT.getLongOpt());
      LOGGER.error(msg);
      return;
    }

    Optional<VertxOptions> vertxOptions = BackendCli.parseVertxOptions(cli);
    if (vertxOptions.isEmpty()) {
      vertxOptions = BackendCli.parseVertxOptionsFile(cli);
    }

    Optional<DeploymentOptions> deploymentOptions = BackendCli.parseVertxDeployOptions(cli);
    if (deploymentOptions.isEmpty()) {
      deploymentOptions = BackendCli.parseVertxDeployOptionsFile(cli);
    }

    IBackend.IBuilder builder = BackendManager.getBackendBuilder().get();
    builder.deployOptions(deployOptions);
    IBackend backend = builder.build();
    /**
     * Backend start here.
     */
    backend.start();
    BackendGateway gateway = new BackendGateway(backend);

    VertxOptions vertxOpts = vertxOptions.orElseGet(VertxOptions::new);
    DeploymentOptions deploymentOpts = deploymentOptions.orElseGet(DeploymentOptions::new);
    deploymentOpts.setConfig(new JsonObject());
    deploymentOpts.getConfig().put("epoch", deployEpoch.get())
        .put("deployId", deployId.get())
        .put("reportServiceAddress", reportServiceAddress.get())
        .put("restApiPort", restApiPort.get());

    ClusterManager clusterManager = new ZookeeperClusterManager("zookeeper.json");
    vertxOpts.setClusterManager(clusterManager);
    Vertx.clusteredVertx(vertxOpts).compose((Vertx vertx) -> {
          return vertx.deployVerticle(gateway, deploymentOpts);
        }).onSuccess(deployID -> {
          String msg = String.format("Success to deploy %s:%s.", BackendGateway.class, deployID);
          LOGGER.info(msg);
        })
        .onFailure(t -> {
          String msg = String.format("Fail to deploy %s.", BackendGateway.class);
          LOGGER.error(msg, t);
        });
  }
}
