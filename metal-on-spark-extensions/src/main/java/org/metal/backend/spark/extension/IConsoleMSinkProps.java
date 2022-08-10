package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMSinkProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableIConsoleMSinkProps.class)
@JsonSerialize(as = ImmutableIConsoleMSinkProps.class)
public interface IConsoleMSinkProps extends IMSinkProps {
    public int numRows();
}
