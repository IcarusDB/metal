package org.metal.backend;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class BackendCliTest {
    @Test
    public void case0() throws ParseException {
        String[] args = {
                "--conf", "master=spark://master-0.spark.node:7077",
                "--conf", "appName=test-2",
                "--conf-file", "src/test/resources/backend.cnf"
        };

        BackendDeployOptions deployOptions = BackendCli.parseDeployOptions(args);
        System.out.println(deployOptions);
    }

    @Test
    public void case1() throws ParseException {
        String[] args = {
                "--conf", "master=spark://master-0.spark.node:7077",
                "--conf", "appName=test-2",
                "--conf-file", "src/test/resources/backend.cnf",
                "--setup", "{\n" +
                "  \"type\" : \"org.metal.backend.BackendCliTest$MockISetup\"\n" +
                "}",
                "--setup-file", "src/test/resources/backend.setup"
        };

        BackendDeployOptions deployOptions = BackendCli.parseDeployOptions(args);
        System.out.println(deployOptions);
    }

    public static class MockISetup implements ISetup<Thread> {

        @Override
        public void setup(Thread platform) {
            System.out.println("Mock ISetup");
        }
    }
}
