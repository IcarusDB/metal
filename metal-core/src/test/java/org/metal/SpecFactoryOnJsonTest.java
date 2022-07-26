package org.metal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;


class MSourceImpl extends MSource<String> {
    public MSourceImpl() {}
    public MSourceImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public String source() {
        return "I am source.";
    }
}

class MMapperImpl extends MMapper<String, String> {
    public MMapperImpl() {}
    public MMapperImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public String map(String data) {
        return "I am mapper";
    }
}

class MFusionImpl extends MFusion<String, String> {
    public MFusionImpl() {}
    public MFusionImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public String fusion(List<String> datas) {
        return "I amm fusion";
    }
}

class MSinkImpl extends MSink<String> {
    public MSinkImpl() {}
    public MSinkImpl(String id, String name) {
        super(id, name);
    }

    @Override
    public void sink(String data) {
        System.out.println("I am sink");
    }
}

public class SpecFactoryOnJsonTest {
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
    public void get() {
        SpecFactory factory = new SpecFactoryOnJson();
        MSourceImpl mSource = new MSourceImpl("00-00", "source-00");
        MMapperImpl mMapper0 = new MMapperImpl("01-00", "mapper-00");
        MMapperImpl mMapper1 = new MMapperImpl("01-01", "mapper-01");
        MFusionImpl mFusion = new MFusionImpl("02-00", "fusion-00");
        MSinkImpl mSink = new MSinkImpl("03-00", "sink-00");

        JsonMapper mapper = new JsonMapper();
        Spec spec = new Spec("1.0");
        spec.getMetals().addAll(List.of(mSource, mMapper0, mMapper1, mFusion, mSink));
        spec.getEdges().addAll(List.of(
                Pair.of(mSource.getId(), mMapper0.getId()),
                Pair.of(mSource.getId(), mMapper1.getId()),
                Pair.of(mMapper0.getId(), mFusion.getId()),
                Pair.of(mMapper1.getId(), mFusion.getId()),
                Pair.of(mFusion.getId(), mSink.getId())
        ));

        try {
            System.out.println(
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void json() {
        SpecFactory factory = new SpecFactoryOnJson();
        try {
            Spec spec = factory.get(json);
            System.out.println(spec);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
