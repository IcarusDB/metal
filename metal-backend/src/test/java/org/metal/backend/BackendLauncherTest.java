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

import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

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

  @Test
  public void case1() throws IOException, ParseException {
    String[] args = {"--help"};
    BackendLauncher.main(args);
  }

//    @Test
//    public void case2() throws IOException, ParseException {
//        String[] args = {
//            "--conf", "master=spark://192.168.41.70:7077",
//            "--conf", "appName=test-2",
//            "--setup", "{\n" +
//            "  \"type\" : \"org.metal.backend.spark.extension.ml.udf.AsVector\",\n" +
//            "  \"name\" : \"as_vector\"\n" +
//            "}",
//            "--cmd-mode",
//            "--spec-file", "/home/cheney/expr/spark/standalone/mini-cluster/spec.json"
////            "--spec-file", "/home/spark/metal/spec.json"
//        };
//        BackendLauncher.main(args);
//    }

  @Test
  public void case2() throws IOException, ParseException {
    String[] args = {
        "--conf", "master=local[*]",
        "--conf", "appName=test-2",
        "--setup", "{\n" +
        "  \"type\" : \"org.metal.backend.spark.extension.ml.udf.AsVector\",\n" +
        "  \"name\" : \"as_vector\"\n" +
        "}",
        "--cmd-mode",
        "--spec-file", "src/test/resources/specFusion.json"
    };
    BackendLauncher.main(args);
  }
}
