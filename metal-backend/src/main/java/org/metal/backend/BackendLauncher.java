package org.metal.backend;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.apache.commons.cli.*;
import org.metal.draft.DraftMaster;
import org.metal.service.BaseMetalService;
import org.metal.specs.Spec;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

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
            String msg = String.format("%s is not set.", BackendCli.REPORT_SERVICE_ADDRESS_OPT.getLongOpt());
            LOGGER.error(msg);
            return;
        }

        String id = deployId + "-" + deployEpoch;
        Optional<Integer> port = BackendCli.parseRestApiPort(cli);
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

        IBackendRESTAPIProps restAPIProps = ImmutableIBackendRESTAPIProps.builder()
            .id(id)
            .port(port)
            .build();
        BackendRESTAPI restAPI = new BackendRESTAPI(restAPIProps, backend);

        VertxOptions vertxOpts = vertxOptions.orElseGet(VertxOptions::new);
        DeploymentOptions deploymentOpts = deploymentOptions.orElseGet(DeploymentOptions::new);
        Vertx vertx = Vertx.vertx(vertxOpts);
        vertx.deployVerticle(restAPI, deploymentOpts)
            .onSuccess(deployID -> {
                String msg = String.format("Success to deploy %s:%s.", BackendRESTAPI.class, deployID);
                LOGGER.info(msg);
            })
            .onFailure(t -> {
                String msg = String.format("Fail to deploy %s.", BackendRESTAPI.class);
                LOGGER.error(msg, t);
            });
    }
}
