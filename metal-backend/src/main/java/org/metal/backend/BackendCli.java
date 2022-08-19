package org.metal.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BackendCli {
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

    public static Options create() {
        Options options = new Options();
        options.addOption(CONF_OPT);
        options.addOption(CONF_FILE_OPT);
        options.addOption(SETUP_OPT);
        options.addOption(SETUP_FILE_OPT);
        options.addOption(CMD_OPT);
        options.addOption(SPEC_OPT);
        options.addOption(SPEC_FILE_OPT);
        options.addOption(INTERACTIVE_OPT);
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
            if (!Files.exists(confPath)) {
                String msg = String.format("%s is not exist.", confPath);
                throw new IllegalArgumentException(msg);
            }
            if (Files.isDirectory(confPath)) {
                String msg = String.format("%s is directory and not a file path.", confPath);
                throw new IllegalArgumentException(msg);
            }

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
        if (!Files.exists(setupPath)) {
            String msg = String.format("%s is not exist.", setupPath);
            throw new IllegalArgumentException(msg);
        }
        if (Files.isDirectory(setupPath)) {
            String msg = String.format("%s is directory and not a file.", setupPath);
            throw new IllegalArgumentException(msg);
        }
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

    public static Optional<Spec> parseSpec(CommandLine cli) throws IllegalArgumentException{
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
        } catch (IOException e) {
            e.printStackTrace();
            String msg = String.format("Fail to get one Spec from %s.", value);
            throw new IllegalArgumentException(msg, e);
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
        if (!Files.exists(specPath)) {
            String msg = String.format("%s is not exist.", specPath);
            throw new IllegalArgumentException(msg);
        }

        if (Files.isDirectory(specPath)) {
            String msg = String.format("%s is one directory and not one file.", specPath);
            throw new IllegalArgumentException(msg);
        }

        try {
            byte[] buffer = Files.readAllBytes(specPath);
            Spec spec = new SpecFactoryOnJson().get(buffer);
            return Optional.<Spec>of(spec);
        } catch (IOException e) {
            String msg = String.format("Fail to get one Spec from %s.", specPath);
            throw new IllegalArgumentException(msg, e);
        }
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
}
