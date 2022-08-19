package org.metal.backend;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;

public class BackendLauncherTest {
    @Test
    public void case0() throws IOException, ParseException {
        String[] args = {
                "--conf", "master=local[*]",
                "--conf", "appName=test-2",
                "--setup", "{\n" +
                "  \"type\" : \"org.metal.backend.spark.extension.ml.udf.AsVector\",\n" +
                "  \"name\" : \"as_vector\"\n" +
                "}",
                "--cmd-mode",
                "--spec-file", "src/test/resources/spec.json"
        };
        BackendLauncher.main(args);
    }
}
