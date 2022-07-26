package org.metal;

import org.junit.Test;

import java.io.IOException;

public class DraftMasterTest {
    private String json = "{\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"metals\" : [ {\n" +
            "    \"type\" : \"org.metal.MSourceImpl\",\n" +
            "    \"id\" : \"00-00\",\n" +
            "    \"name\" : \"source-00\"\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.MMapperImpl\",\n" +
            "    \"id\" : \"01-00\",\n" +
            "    \"name\" : \"mapper-00\"\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.MMapperImpl\",\n" +
            "    \"id\" : \"01-01\",\n" +
            "    \"name\" : \"mapper-01\"\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.MFusionImpl\",\n" +
            "    \"id\" : \"02-00\",\n" +
            "    \"name\" : \"fusion-00\"\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.MSinkImpl\",\n" +
            "    \"id\" : \"03-00\",\n" +
            "    \"name\" : \"sink-00\"\n" +
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
