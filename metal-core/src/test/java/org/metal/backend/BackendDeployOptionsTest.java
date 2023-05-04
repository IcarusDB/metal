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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class BackendDeployOptionsTest {

  private static class MockISetup implements ISetup<Thread> {

    @Override
    public void setup(Thread platform) {

    }
  }

  @Test
  public void case0() throws JsonProcessingException {
    BackendDeployOptions deployOptions = new BackendDeployOptions();
    deployOptions.getConfs().put("master", "spark://master-0.spark.node:7077");
    deployOptions.getConfs().put("appName", "Test");
    deployOptions.getSetups().add(new MockISetup());

    ObjectMapper mapper = new ObjectMapper();
    String val = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(deployOptions);
    System.out.println(val);
  }

  @Test
  public void case1() throws JsonProcessingException {
    String json = "{\n" +
        "  \"confs\" : {\n" +
        "    \"appName\" : \"Test\",\n" +
        "    \"master\" : \"spark://master-0.spark.node:7077\"\n" +
        "  },\n" +
        "  \"setups\" : [ {\n" +
        "    \"type\" : \"org.metal.backend.BackendDeployOptionsTest$MockISetup\"\n" +
        "  } ]\n" +
        "}";

    ObjectMapper mapper = new ObjectMapper();
    BackendDeployOptions<Thread> deployOptions = mapper.readValue(json, BackendDeployOptions.class);
    System.out.println(deployOptions);
  }
}
