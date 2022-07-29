package org.metal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.props.IMFusionProps;
import org.metal.props.IMMapperProps;
import org.metal.props.IMSinkProps;
import org.metal.props.IMSourceProps;

import java.util.List;

public class Mock {
    public static class MFusionImpl extends MFusion<String, Thread, MFusionPropsFoo> {

        @JsonCreator
        public MFusionImpl(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("props") MFusionPropsFoo props) {
            super(id, name, props);
        }

        @Override
        public String fusion(List<String> datas) {
            return "I amm fusion";
        }
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableMFusionPropsFoo.class)
    @JsonSerialize(as = ImmutableMFusionPropsFoo.class)
    public static
    interface MFusionPropsFoo extends IMFusionProps {

    }

    public static class MMapperImpl extends MMapper<String, Thread, MMapperPropsFoo> {

        @JsonCreator
        public MMapperImpl(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("props") MMapperPropsFoo props) {
            super(id, name, props);
        }

        @Override
        public String map(String data) {
            return "I am mapper";
        }
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableMMapperPropsFoo.class)
    @JsonSerialize(as = ImmutableMMapperPropsFoo.class)
    public static
    interface MMapperPropsFoo extends IMMapperProps {

    }

    public static class MSinkImpl extends MSink<String, Thread, MSinkPropsFoo> {

        @JsonCreator
        public MSinkImpl(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("props") MSinkPropsFoo props) {
            super(id, name, props);
        }

        @Override
        public void sink(String data) {
            System.out.println("I am sink");
        }
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableMSinkPropsFoo.class)
    @JsonSerialize(as = ImmutableMSinkPropsFoo.class)
    public static
    interface MSinkPropsFoo extends IMSinkProps {

    }

    public static class MSourceImpl extends MSource<String, Thread, MSourcePropsFoo> {
        @JsonCreator
        public MSourceImpl(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("props") MSourcePropsFoo props) {
            super(id, name, props);
        }

        @Override
        public String source() {
            return "I am source.";
        }
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableMSourcePropsFoo.class)
    @JsonSerialize(as = ImmutableMSourcePropsFoo.class)
    public static
    interface MSourcePropsFoo extends IMSourceProps {

    }
}
