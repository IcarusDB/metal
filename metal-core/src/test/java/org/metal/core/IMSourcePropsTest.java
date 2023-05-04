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

package org.metal.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.junit.Test;
import org.metal.core.props.IMSourceProps;

public class IMSourcePropsTest {

  @Value.Immutable
  @JsonDeserialize(as = ImmutableIMSourcePropsFoo.class)
  @JsonSerialize(as = ImmutableIMSourcePropsFoo.class)
  static interface IMSourcePropsFoo extends IMSourceProps {

  }

  @Test
  public void testSer() {
    IMSourcePropsFoo properties = ImmutableIMSourcePropsFoo.builder()
        .schema("{}")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    try {
      System.out.println(
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properties)
      );
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testDeser() {
    String json = "{\n" +
        "  \"schema\" : \"{}\"\n" +
        "}";
    ObjectMapper mapper = new ObjectMapper();
    try {
      System.out.println(
          mapper.readValue(json, IMSourcePropsFoo.class)
      );
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }


  public static class Outter {

    public String z;
    public IMSourcePropsFoo properties;
  }

  @Test
  public void testInnerSer() {
    IMSourcePropsFoo properties = ImmutableIMSourcePropsFoo.builder()
        .schema("{}")
        .build();
    Outter outter = new Outter();
    outter.z = "zz";
    outter.properties = properties;

    ObjectMapper mapper = new ObjectMapper();
    try {
      System.out.println(
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outter)
      );
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testInnerDesr() {
    String json = "{\n" +
        "  \"z\" : \"zz\",\n" +
        "  \"properties\" : {\n" +
        "    \"schema\" : \"{}\"\n" +
        "  }\n" +
        "}";

    ObjectMapper mapper = new ObjectMapper();
    try {
      System.out.println(
          mapper.readValue(json, Outter.class)
      );
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
