package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMMapperProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableIWhereMMapperProps.class)
@JsonSerialize(as = ImmutableIWhereMMapperProps.class)
public interface IWhereMMapperProps extends IMMapperProps {
    public String conditionExpr();
}
