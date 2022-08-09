package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMSinkProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableIConsoleSparkMSinkProps.class)
@JsonSerialize(as = ImmutableIConsoleSparkMSinkProps.class)
public interface IConsoleSparkMSinkProps extends IMSinkProps {
    public int numRows();
}
