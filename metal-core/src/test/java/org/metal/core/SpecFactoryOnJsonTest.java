package org.metal.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.metal.core.specs.Spec;
import org.metal.core.specs.SpecFactory;
import org.metal.core.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.util.List;

public class SpecFactoryOnJsonTest {
    @Test
    public void testSer() throws JsonProcessingException {
        Mock.MSourcePropsFoo mSourcePropsFoo = ImmutableMSourcePropsFoo.builder().schema("{}").build();
        Mock.MMapperPropsFoo mMapperPropsFoo0 = ImmutableMMapperPropsFoo.builder().build();
        Mock.MMapperPropsFoo mMapperPropsFoo1 = ImmutableMMapperPropsFoo.builder().build();
        Mock.MFusionPropsFoo mFusionPropsFoo = ImmutableMFusionPropsFoo.builder().build();
        Mock.MSinkPropsFoo mSinkPropsFoo = ImmutableMSinkPropsFoo.builder().build();

        Mock.MSourceImpl mSource = new Mock.MSourceImpl("00-00", "source-00", mSourcePropsFoo);
        Mock.MMapperImpl mMapper0 = new Mock.MMapperImpl("01-00", "mapper-00", mMapperPropsFoo0);
        Mock.MMapperImpl mMapper1 = new Mock.MMapperImpl("01-01", "mapper-01", mMapperPropsFoo1);
        Mock.MFusionImpl mFusion = new Mock.MFusionImpl("02-00", "fusion-00", mFusionPropsFoo);
        Mock.MSinkImpl mSink = new Mock.MSinkImpl("03-00", "sink-00", mSinkPropsFoo);

        Spec spec = new Spec("0.0.1");
        spec.getMetals().addAll(List.of(mSource, mMapper0, mMapper1, mFusion, mSink));
        spec.getEdges().add(Pair.of("00-00", "01-00"));
        spec.getEdges().add(Pair.of("00-00", "01-01"));
        spec.getEdges().add(Pair.of("01-00", "02-00"));
        spec.getEdges().add(Pair.of("01-01", "02-00"));
        spec.getEdges().add(Pair.of("02-00", "03-00"));

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec)
        );
    }

    @Test
    public void testDeser() throws IOException {
        String json = "{\n" +
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

        SpecFactory factory = new SpecFactoryOnJson();
        Spec spec = factory.get(json);
        System.out.println(spec);
        System.out.println(spec.getVersion());
        System.out.println(spec.getEdges());
        System.out.println(spec.getMetals());
        spec.getMetals().forEach(metal -> {
            System.out.println(metal.props());
        });
    }

}
