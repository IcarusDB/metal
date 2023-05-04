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

import org.junit.Test;

public class BackendDeployManagerTest {

  @Test
  public void case0() {
    System.out.println(BackendDeployManager.getBackendDeploy().get());
  }

  @Test
  public void case1() {
    String[] args = {
        "--class", "org.metal.backend.BackendLauncher",
        "--master", "local[*]",
        "--conf", "spark.executor.userClassPathFirst=true",
        "--conf", "spark.driver.userClassPathFirst=true",
        "--packages",
        "org.metal:metal-core:1.0-SNAPSHOT,org.metal:metal-backend:1.0-SNAPSHOT,org.metal:metal-on-spark:1.0-SNAPSHOT,org.metal:metal-on-spark-extensions:1.0-SNAPSHOT",
        "../libs/metal-backend-1.0-SNAPSHOT.jar",
        "--conf", "appName=Test",
        "--cmd-mode",
        "--spec-file",
        "./src/test/resources/spec.json"
    };
    BackendDeployManager.getBackendDeploy().get().deploy(args);
  }

}
