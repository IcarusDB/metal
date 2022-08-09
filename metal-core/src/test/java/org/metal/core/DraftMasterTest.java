package org.metal.core;

import com.google.common.graph.Traverser;
import org.junit.Test;
import org.metal.core.draft.Draft;
import org.metal.core.draft.DraftMaster;
import org.metal.core.specs.SpecFactory;
import org.metal.core.specs.SpecFactoryOnJson;

import java.io.IOException;

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
            Traverser.forGraph(draft.getGraph()).breadthFirst(draft.getSources()).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
