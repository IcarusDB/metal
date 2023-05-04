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

import com.google.common.graph.Traverser;
import java.io.IOException;
import org.junit.Test;
import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.specs.SpecFactory;
import org.metal.specs.SpecFactoryOnJson;

public class DraftMasterTest {

  private String json = "{\n" +
      "  \"version\" : \"0.0.1\",\n" +
      "  \"metals\" : [ {\n" +
      "    \"type\" : \"org.metal.core.Mock$MSourceImpl\",\n" +
      "    \"id\" : \"00-00\",\n" +
      "    \"name\" : \"source-00\",\n" +
      "    \"props\" : {\n" +
      "      \"schema\" : \"{}\"\n" +
      "    }\n" +
      "  }, {\n" +
      "    \"type\" : \"org.metal.core.Mock$MMapperImpl\",\n" +
      "    \"id\" : \"01-00\",\n" +
      "    \"name\" : \"mapper-00\",\n" +
      "    \"props\" : { }\n" +
      "  }, {\n" +
      "    \"type\" : \"org.metal.core.Mock$MMapperImpl\",\n" +
      "    \"id\" : \"01-01\",\n" +
      "    \"name\" : \"mapper-01\",\n" +
      "    \"props\" : { }\n" +
      "  }, {\n" +
      "    \"type\" : \"org.metal.core.Mock$MFusionImpl\",\n" +
      "    \"id\" : \"02-00\",\n" +
      "    \"name\" : \"fusion-00\",\n" +
      "    \"props\" : { }\n" +
      "  }, {\n" +
      "    \"type\" : \"org.metal.core.Mock$MSinkImpl\",\n" +
      "    \"id\" : \"03-00\",\n" +
      "    \"name\" : \"sink-00\",\n" +
      "    \"props\" : { }\n" +
      "  } ],\n" +
      "  \"edges\" : [ {\n" +
      "    \"left\" : \"00-00\",\n" +
      "    \"right\" : \"01-00\"\n" +
      "  }, {\n" +
      "    \"left\" : \"00-00\",\n" +
      "    \"right\" : \"01-01\"\n" +
      "  }, {\n" +
      "    \"left\" : \"01-00\",\n" +
      "    \"right\" : \"02-00\"\n" +
      "  }, {\n" +
      "    \"left\" : \"01-01\",\n" +
      "    \"right\" : \"02-00\"\n" +
      "  }, {\n" +
      "    \"left\" : \"02-00\",\n" +
      "    \"right\" : \"03-00\"\n" +
      "  } ]\n" +
      "}";

  @Test
  public void draft() {
    SpecFactory factory = new SpecFactoryOnJson();
    try {
      Draft draft = DraftMaster.draft(factory.get(json));
      System.out.println(draft);
      Traverser.forGraph(draft.getGraph()).breadthFirst(draft.getSources())
          .forEach(System.out::println);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
