package org.metal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;
import org.immutables.value.Value;
import org.junit.Test;
import org.metal.props.IMMapperProps;
import org.metal.props.IMSinkProps;
import org.metal.props.IMSourceProps;
import org.metal.props.IMetalProps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class TraceIdTest {

    @Value.Immutable
    interface FooMSourceProps extends IMSourceProps{

    }

    @Value.Immutable
    interface FooMSinkProps extends IMSinkProps {

    }

    @Value.Immutable
    interface FooMMapperProps extends IMMapperProps {

    }

    private String traceId(IMetalProps props) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(props);
        return Hashing.sha512().hashBytes(buffer.toByteArray()).toString();
    }

    @Test
    public void testTrace() {
        FooMSourceProps sourceProps0 = ImmutableFooMSourceProps.builder().schema("{}").build();
        FooMSourceProps sourceProps1 = ImmutableFooMSourceProps.builder().schema("").build();

        FooMSinkProps sinkProps0 = ImmutableFooMSinkProps.builder().build();
        FooMSinkProps sinkProps1 = ImmutableFooMSinkProps.builder().build();

        FooMMapperProps mapperProps0 = ImmutableFooMMapperProps.builder().build();
        FooMMapperProps mapperProps1 = ImmutableFooMMapperProps.builder().build();

        try {
            System.out.println(traceId(sourceProps0));
            System.out.println(traceId(sourceProps1));

            System.out.println(traceId(sinkProps0));
            System.out.println(traceId(sinkProps1));

            System.out.println(traceId(mapperProps0));
            System.out.println(traceId(mapperProps1));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
