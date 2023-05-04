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

package org.metal.backend.spark.extension.ml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.metal.backend.ISetup;
import org.metal.backend.spark.extension.ml.udf.AsVector;

public class AsVectorTest {

  @Test
  public void case0() throws JsonProcessingException {
    AsVector asVector = new AsVector();
    ObjectMapper mapper = new ObjectMapper();
    String val = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(asVector);
    System.out.println(val);
  }

  @Test
  public void case1() throws JsonProcessingException {
    String json = "{\n" +
        "  \"type\" : \"org.metal.backend.spark.extension.ml.udf.AsVector\"\n" +
        "}";
    ObjectMapper mapper = new ObjectMapper();
    AsVector asVector = (AsVector) mapper.readValue(json, ISetup.class);
    System.out.println(asVector.getName());
  }
}
