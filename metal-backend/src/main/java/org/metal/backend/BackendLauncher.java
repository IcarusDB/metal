package org.metal.backend;

import org.apache.commons.cli.*;
import org.metal.draft.DraftMaster;
import org.metal.service.BaseMetalService;
import org.metal.specs.Spec;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class BackendLauncher {
    static String json = "{\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"metals\" : [ {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.JsonFileMSource\",\n" +
            "    \"id\" : \"00-00\",\n" +
            "    \"name\" : \"source-00\",\n" +
            "    \"props\" : {\n" +
            "      \"schema\" : \"\",\n" +
            "      \"path\" : \"hdfs://namenode.hdfs.node:9000/test.json\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.ConsoleMSink\",\n" +
            "    \"id\" : \"01-00\",\n" +
            "    \"name\" : \"sink-00\",\n" +
            "    \"props\" : {\n" +
            "      \"numRows\" : 10\n" +
            "    }\n" +
            "  } ],\n" +
            "  \"edges\" : [ {\n" +
            "    \"left\" : \"00-00\",\n" +
            "    \"right\" : \"01-00\"\n" +
            "  } ],\n" +
            "  \"waitFor\" : [ ]\n" +
            "}";

    public static void main(String[] args) throws IOException, ParseException {
        Options options = BackendCli.create();
        CommandLine cli = BackendCli.parser(args, options);
        BackendDeployOptions deployOptions = BackendCli.parseDeployOptions(cli);
        System.out.println(deployOptions);
        tryRunCMD(cli, deployOptions);
    }

    private static void tryRunCMD(CommandLine cli, BackendDeployOptions deployOptions) {
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
}
