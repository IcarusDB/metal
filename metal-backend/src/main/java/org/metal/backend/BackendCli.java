package org.metal.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.apache.commons.cli.*;
import org.metal.exception.MetalSpecParseException;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BackendCli {
    public final static Option HELP_OPT = Option.builder()
        .longOpt("help")
        .build();

    public final static Option CONF_OPT = Option.builder()
            .option("c")
            .longOpt("conf")
            .hasArgs()
            .valueSeparator('=')
            .desc("Backend Deploy Options conf in 'key=value' format.")
            .build();

    public final static Option CONF_FILE_OPT = Option.builder()
            .option("C")
            .longOpt("conf-file")
            .hasArg()
            .desc("Backend Deploy Options conf file path.")
            .build();

    public final static Option SETUP_OPT = Option.builder()
            .option("s")
            .longOpt("setup")
            .hasArgs()
            .desc("Backend Deploy Options conf in json format.")
            .build();

    public final static Option SETUP_FILE_OPT = Option.builder()
            .option("S")
            .longOpt("setup-file")
            .hasArg()
            .desc("Backend Deploy Options conf file path.")
            .build();

    public final static Option CMD_OPT = Option.builder()
            .longOpt("cmd-mode")
            .hasArg(false)
            .desc("When CMD mode is enable, Backend will analysis and execute metal SPEC. This mode will not start interactive service.")
            .build();

    public final static Option SPEC_OPT = Option.builder()
            .longOpt("spec")
            .hasArg()
            .desc("When CMD mode is enable, this option is used to set metal SPEC. And this option will lead to ignore --spec-file option.")
            .build();

    public final static Option SPEC_FILE_OPT = Option.builder()
            .longOpt("spec-file")
            .hasArg()
            .desc("When CMD mode is enable, this option is used to set metal SPEC file path.")
            .build();

    public final static Option INTERACTIVE_OPT = Option.builder()
            .longOpt("interactive-mode")
            .desc("When INTERACTIVE mode is enable, Backend will start all related services. This option will lead to ignore --cmd-mode option.")
            .build();

    public final static Option DEPLOY_ID_OPT = Option.builder()
        .longOpt("deploy-id")
        .desc("When INTERACTIVE mode is enable, this option is used to set backend service id part. Service id is '${deploy-id}-${deploy-epoch}'.")
        .build();

    public final static Option DEPLOY_EPOCH_OPT = Option.builder()
        .longOpt("deploy-epoch")
        .desc("When INTERACTIVE mode is enable, this option is used to set backend service id part. Service id is '${deploy-id}-${deploy-epoch}'.")
        .build();

    public final static Option REPORT_SERVICE_ADDRESS_OPT = Option.builder()
        .longOpt("report-service-address")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option is used to report status of backend and executed long term task.")
        .build();

    public final static Option REST_API_PORT_OPT = Option.builder()
        .longOpt("rest-api-port")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option is used to set backend service rest api port.")
        .build();

    public final static Option VERTX_OPTIONS_OPT = Option.builder()
        .longOpt("vertx-options")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option in json format is used to configure vertx.")
        .build();

    public final static Option VERTX_OPTIONS_FILE_OPT = Option.builder()
        .longOpt("vertx-options-file")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option about file path is used to configure vertx.")
        .build();

    public final static Option VERTX_DEPLOY_OPT = Option.builder()
        .longOpt("vertx-deploy")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option in json format is used to configure vertx deployment.")
        .build();

    public final static Option VERTX_DEPLOY_FILE_OPT = Option.builder()
        .longOpt("vertx-deploy-file")
        .hasArg()
        .desc("When INTERACTIVE mode is enable, this option about file path is used to configure vertx deployment.")
        .build();

    public static Options create() {
        Options options = new Options();
        options.addOption(HELP_OPT);
        options.addOption(CONF_OPT);
        options.addOption(CONF_FILE_OPT);
        options.addOption(SETUP_OPT);
        options.addOption(SETUP_FILE_OPT);
        options.addOption(CMD_OPT);
        options.addOption(SPEC_OPT);
        options.addOption(SPEC_FILE_OPT);
        options.addOption(INTERACTIVE_OPT);
        options.addOption(DEPLOY_ID_OPT);
        options.addOption(DEPLOY_EPOCH_OPT);
        options.addOption(REPORT_SERVICE_ADDRESS_OPT);
        options.addOption(REST_API_PORT_OPT);
        options.addOption(VERTX_OPTIONS_OPT);
        options.addOption(VERTX_OPTIONS_FILE_OPT);
        options.addOption(VERTX_DEPLOY_OPT);
        options.addOption(VERTX_DEPLOY_FILE_OPT);
        return options;
    }

    public static CommandLine parser(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    public static BackendDeployOptions parseDeployOptions(String args[]) throws IllegalArgumentException{
        Options options = create();
        try {
            CommandLine cli = parser(args, options);
            return parseDeployOptions(cli);
        } catch (ParseException e) {
            String msg = String.format("Fail to parse args:%s.", Arrays.asList(args));
            throw new IllegalArgumentException(e);
        }
    }

    public static BackendDeployOptions parseDeployOptions(CommandLine cli) {
        BackendDeployOptions deployOptions = new BackendDeployOptions();
        deployOptions.getConfs().putAll(parseConfFile(cli));
        deployOptions.getConfs().putAll(parseConf(cli));
        deployOptions.getSetups().addAll(parseSetupFile(cli));
        deployOptions.getSetups().addAll(parseSetup(cli));

        return deployOptions;
    }

    private static void validConfPath(Path confPath) throws IllegalArgumentException{
        if (!Files.exists(confPath)) {
            String msg = String.format("%s is not exist.", confPath);
            throw new IllegalArgumentException(msg);
        }
        if (Files.isDirectory(confPath)) {
            String msg = String.format("%s is directory and not a file path.", confPath);
            throw new IllegalArgumentException(msg);
        }
    }

    public static Map<String, Object> parseConf(CommandLine cli) throws IllegalArgumentException{
        Map<String, Object> confs = new HashMap<>();
        if (cli.hasOption(CONF_OPT)) {
            String[] values = cli.getOptionValues(CONF_OPT);
            for(int idx = 0; idx < values.length; idx += 2) {
                try {
                    confs.put(values[idx], values[idx + 1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    String msg = String.format("Fail to parse key-value pairs from %s.", Arrays.asList(values));
                    throw new IllegalArgumentException(msg, e);
                }
            }
        }
        return confs;
    }

    public static Map<String, Object> parseConfFile(CommandLine cli) throws IllegalArgumentException{
        Map<String, Object> confs = new HashMap<>();
        if (cli.hasOption(CONF_FILE_OPT)) {
            Path confPath = Paths.get(cli.getOptionValue(CONF_FILE_OPT));
            validConfPath(confPath);

            try {
                List<String> lines = Files.readAllLines(confPath);
                for(String line : lines) {
                    String[] kv = line.strip().split(" ", 2);
                    try {
                        String key = kv[0].strip();
                        String val = kv[1].strip();
                        confs.put(key, val);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        String msg = String.format("In %s, %s is not formatted like 'key val'", confPath, line);
                        throw new IllegalArgumentException(msg, e);
                    }
                }
            } catch (IOException e) {
                String msg = String.format("Fail to read data from %s.", confPath);
                throw new IllegalArgumentException(msg, e);
            }
        }

        return confs;
    }

    public static List<ISetup> parseSetup(CommandLine cli) throws IllegalArgumentException{
        if (!cli.hasOption(SETUP_OPT)) {
            return List.of();
        }

        String[] setupOpts = cli.getOptionValues(SETUP_OPT);
        List<ISetup> setups = new ArrayList<>();

        for(String setupOpt: setupOpts) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                ISetup setupImpl = mapper.readValue(setupOpt, ISetup.class);
                setups.add(setupImpl);
            } catch (JsonProcessingException e) {
                String msg = String.format("Fail to parse one Json object into %s from %s.", ISetup.class, setupOpt);
                throw new IllegalArgumentException(msg, e);
            }
        }

        return setups;
    }

    public static List<ISetup> parseSetupFile(CommandLine cli) throws IllegalArgumentException{
        if (!cli.hasOption(SETUP_FILE_OPT)) {
            return List.of();
        }
        List<ISetup> setups = new ArrayList<>();
        Path setupPath = Paths.get(cli.getOptionValue(SETUP_FILE_OPT));
        validConfPath(setupPath);

        try {
            byte[] buffer = Files.readAllBytes(setupPath);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(buffer);
            if (!tree.isArray()) {
                String msg = "All ISetup should be in one Array.";
                throw new IllegalArgumentException(msg);
            }
            Iterator<JsonNode> iterator = tree.iterator();
            while (iterator.hasNext()) {
                JsonNode node = iterator.next();
                ISetup setupImpl = mapper.treeToValue(node, ISetup.class);
                setups.add(setupImpl);
            }
        } catch (IOException e) {
            String msg = String.format("Fail to read data from %s.", setupPath);
            throw new IllegalArgumentException(msg, e);
        }

        return setups;
    }

    public static Optional<Spec> parseSpec(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(CMD_OPT)) {
            return Optional.<Spec>empty();
        }

        if (!cli.hasOption(SPEC_OPT)) {
            return Optional.<Spec>empty();
        }

        String value = cli.getOptionValue(SPEC_OPT);
        try {
            Spec spec = new SpecFactoryOnJson().get(value);
            return Optional.<Spec>of(spec);
        } catch (MetalSpecParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<Spec> parseSpecFile(CommandLine cli) throws IllegalArgumentException{
        if (!cli.hasOption(CMD_OPT)) {
            return Optional.<Spec>empty();
        }

        if (cli.hasOption(SPEC_OPT)) {
            return Optional.<Spec>empty();
        }

        if (!cli.hasOption(SPEC_FILE_OPT)) {
            return Optional.<Spec>empty();
        }

        Path specPath = Paths.get(cli.getOptionValue(SPEC_FILE_OPT));
        validConfPath(specPath);

        try {
            byte[] buffer = Files.readAllBytes(specPath);
            Spec spec = new SpecFactoryOnJson().get(buffer);
            return Optional.<Spec>of(spec);
        } catch (IOException | MetalSpecParseException e) {
            String msg = String.format("Fail to get one Spec from %s.", specPath);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public static Optional<String> parseDeployId(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<String>empty();
        }
        if (!cli.hasOption(DEPLOY_ID_OPT)) {
            return Optional.<String>empty();
        }
        String deployId = cli.getOptionValue(DEPLOY_ID_OPT);
        try {
            return Optional.<String>of(deployId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<Integer> parseDeployEpoch(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<Integer>empty();
        }
        if (!cli.hasOption(DEPLOY_EPOCH_OPT)) {
            return Optional.<Integer>empty();
        }
        String deployEpoch = cli.getOptionValue(DEPLOY_EPOCH_OPT);
        try {
            return Optional.<Integer>of(Integer.valueOf(deployEpoch));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<String> parseReportServiceAddress(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<String>empty();
        }
        if (!cli.hasOption(REPORT_SERVICE_ADDRESS_OPT)) {
            return Optional.<String>empty();
        }
        String reportServiceAddress = cli.getOptionValue(REPORT_SERVICE_ADDRESS_OPT);
        try {
            return Optional.<String>of(reportServiceAddress);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<Integer> parseRestApiPort(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<Integer>empty();
        }
        if (!cli.hasOption(REST_API_PORT_OPT)) {
            return Optional.<Integer>empty();
        }
        String port = cli.getOptionValue(REST_API_PORT_OPT);
        try {
            return Optional.<Integer>of(Integer.valueOf(port));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<VertxOptions> parseVertxOptions(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<VertxOptions>empty();
        }
        if (!cli.hasOption(VERTX_OPTIONS_OPT)) {
            return Optional.<VertxOptions>empty();
        }
        String payload = cli.getOptionValue(VERTX_OPTIONS_OPT);
        try {
            JsonObject json = new JsonObject(payload);
            VertxOptions vertxOptions = new VertxOptions(json);
            return Optional.<VertxOptions>of(vertxOptions);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<VertxOptions> parseVertxOptionsFile(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<VertxOptions>empty();
        }
        if (cli.hasOption(VERTX_OPTIONS_OPT)) {
            return Optional.<VertxOptions>empty();
        }
        if (!cli.hasOption(VERTX_OPTIONS_FILE_OPT)) {
            return Optional.<VertxOptions>empty();
        }

        Path optPath = Paths.get(cli.getOptionValue(VERTX_OPTIONS_FILE_OPT));
        validConfPath(optPath);

        try {
            JsonObject json  = new JsonObject(Files.readString(optPath));
            VertxOptions vertxOptions = new VertxOptions(json);
            return Optional.<VertxOptions>of(vertxOptions);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<DeploymentOptions> parseVertxDeployOptions(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<DeploymentOptions>empty();
        }
        if (!cli.hasOption(VERTX_DEPLOY_OPT)) {
            return Optional.<DeploymentOptions>empty();
        }

        String payload = cli.getOptionValue(VERTX_DEPLOY_OPT);
        try {
            JsonObject json = new JsonObject(payload);
            DeploymentOptions deploymentOptions = new DeploymentOptions(json);
            return Optional.<DeploymentOptions>of(deploymentOptions);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Optional<DeploymentOptions> parseVertxDeployOptionsFile(CommandLine cli) throws IllegalArgumentException {
        if (!cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<DeploymentOptions>empty();
        }
        if (cli.hasOption(VERTX_DEPLOY_OPT)) {
            return Optional.<DeploymentOptions>empty();
        }
        if (!cli.hasOption(VERTX_DEPLOY_FILE_OPT)) {
            return Optional.<DeploymentOptions>empty();
        }

        Path optPath = Paths.get(cli.getOptionValue(VERTX_DEPLOY_FILE_OPT));
        validConfPath(optPath);

        try {
            JsonObject json = new JsonObject(Files.readString(optPath));
            DeploymentOptions deploymentOptions = new DeploymentOptions(json);
            return Optional.<DeploymentOptions>of(deploymentOptions);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isCmdMode(CommandLine cli) {
        if (!cli.hasOption(CMD_OPT)) {
            return false;
        }
        if (cli.hasOption(INTERACTIVE_OPT)) {
            return false;
        }
        return true;
    }

    public static boolean isInteractiveMode(CommandLine cli) {
        return cli.hasOption(INTERACTIVE_OPT);
    }

    public static Optional<Spec> tryCmdMode(CommandLine cli) {
        if (cli.hasOption(INTERACTIVE_OPT)) {
            return Optional.<Spec>empty();
        }

        if (!cli.hasOption(CMD_OPT)) {
            return Optional.<Spec>empty();
        }
        Optional<Spec> optionalSpec = parseSpec(cli);
        if (optionalSpec.isEmpty()) {
            optionalSpec = parseSpecFile(cli);
        }
        return optionalSpec;
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BackendCli", create());
    }
}
