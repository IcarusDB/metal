package org.metal.backend;

import org.junit.Test;

import java.nio.file.Files;

public class BackendDeployManagerTest {
    @Test
    public void case0() {
        System.out.println(BackendDeployManager.getBackendDeploy().get());
    }

    public void case1() {
        String[] args = {
                "--class", "org.metal.backend.BackendLauncher",
                "--master", "local[*]",
                "--conf", "spark.executor.userClassPathFirst=true",
                "--conf", "spark.driver.userClassPathFirst=true",
                "--packages", "org.metal:metal-core:1.0-SNAPSHOT,org.metal:metal-backend:1.0-SNAPSHOT,org.metal:metal-on-spark:1.0-SNAPSHOT,org.metal:metal-on-spark-extensions:1.0-SNAPSHOT",
                "../libs/metal-backend-1.0-SNAPSHOT.jar",
                "--conf", "appName=Test",
                "--cmd-mode",
                "--spec-file",
                "./src/test/resources/spec.json"
        };
        BackendDeployManager.getBackendDeploy().get().deploy(args);
    }
}
