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

import org.apache.commons.cli.ParseException;

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
            "--setup",
                    "{\n" + "  \"type\" : \"org.metal.backend.BackendCliTest$MockISetup\"\n" + "}",
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
