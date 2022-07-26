package org.metal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.immutables.value.Value;
import org.junit.Test;
import org.metal.props.IMFusionProps;
import org.metal.props.IMMapperProps;
import org.metal.props.IMSinkProps;
import org.metal.props.IMSourceProps;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactory;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableMSourcePropsFoo.class)
@JsonSerialize(as = ImmutableMSourcePropsFoo.class)
interface MSourcePropsFoo extends IMSourceProps{

}

@Value.Immutable
@JsonDeserialize(as = ImmutableMMapperPropsFoo.class)
@JsonSerialize(as = ImmutableMMapperPropsFoo.class)
interface MMapperPropsFoo extends IMMapperProps{

}

@Value.Immutable
@JsonDeserialize(as = ImmutableMSinkPropsFoo.class)
@JsonSerialize(as = ImmutableMSinkPropsFoo.class)
interface MSinkPropsFoo extends IMSinkProps {

}

@Value.Immutable
@JsonDeserialize(as = ImmutableMFusionPropsFoo.class)
@JsonSerialize(as = ImmutableMFusionPropsFoo.class)
interface MFusionPropsFoo extends IMFusionProps {

}

class MSourceImpl extends MSource<String, MSourcePropsFoo> {
    @JsonCreator
    public MSourceImpl(@JsonProperty("props") MSourcePropsFoo props) {
        super(props);
    }

    @Override
    public String source() {
        return "I am source.";
    }
}

class MMapperImpl extends MMapper<String, String, MMapperPropsFoo> {

    @JsonCreator
    public MMapperImpl(@JsonProperty("props") MMapperPropsFoo props) {
        super(props);
    }

    @Override
    public String map(String data) {
        return "I am mapper";
    }
}

class MFusionImpl extends MFusion<String, String, MFusionPropsFoo> {

    @JsonCreator
    public MFusionImpl(@JsonProperty("props") MFusionPropsFoo props) {
        super(props);
    }

    @Override
    public String fusion(List<String> datas) {
        return "I amm fusion";
    }
}

class MSinkImpl extends MSink<String, MSinkPropsFoo> {

    @JsonCreator
    public MSinkImpl(@JsonProperty("props") MSinkPropsFoo props) {
        super(props);
    }

    @Override
    public void sink(String data) {
        System.out.println("I am sink");
    }
}

public class SpecFactoryOnJsonTest {
    @Test
    public void testSer() throws JsonProcessingException {
        MSourcePropsFoo mSourcePropsFoo = ImmutableMSourcePropsFoo.builder().id("00-00").name("source-00").schema("{}").build();
        MMapperPropsFoo mMapperPropsFoo0 = ImmutableMMapperPropsFoo.builder().id("01-00").name("mapper-00").build();
        MMapperPropsFoo mMapperPropsFoo1 = ImmutableMMapperPropsFoo.builder().id("01-01").name("mapper-01").build();
        MFusionPropsFoo mFusionPropsFoo = ImmutableMFusionPropsFoo.builder().id("02-00").name("fusion-00").build();
        MSinkPropsFoo mSinkPropsFoo = ImmutableMSinkPropsFoo.builder().id("03-00").name("sink-00").build();

        MSourceImpl mSource = new MSourceImpl(mSourcePropsFoo);
        MMapperImpl mMapper0 = new MMapperImpl(mMapperPropsFoo0);
        MMapperImpl mMapper1 = new MMapperImpl(mMapperPropsFoo1);
        MFusionImpl mFusion = new MFusionImpl(mFusionPropsFoo);
        MSinkImpl mSink = new MSinkImpl(mSinkPropsFoo);

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
                "    \"type\" : \"org.metal.MSourceImpl\",\n" +
                "    \"props\" : {\n" +
                "      \"id\" : \"00-00\",\n" +
                "      \"name\" : \"source-00\",\n" +
                "      \"schema\" : \"{}\"\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"type\" : \"org.metal.MMapperImpl\",\n" +
                "    \"props\" : {\n" +
                "      \"id\" : \"01-00\",\n" +
                "      \"name\" : \"mapper-00\"\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"type\" : \"org.metal.MMapperImpl\",\n" +
                "    \"props\" : {\n" +
                "      \"id\" : \"01-01\",\n" +
                "      \"name\" : \"mapper-01\"\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"type\" : \"org.metal.MFusionImpl\",\n" +
                "    \"props\" : {\n" +
                "      \"id\" : \"02-00\",\n" +
                "      \"name\" : \"fusion-00\"\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"type\" : \"org.metal.MSinkImpl\",\n" +
                "    \"props\" : {\n" +
                "      \"id\" : \"03-00\",\n" +
                "      \"name\" : \"sink-00\"\n" +
                "    }\n" +
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
                "}\n";
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
