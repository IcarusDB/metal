package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.backend.spark.extension.ImmutableIJsonFileSparkMSourceProps;
import org.metal.core.props.IMSourceProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableIJsonFileSparkMSourceProps.class)
@JsonSerialize(as = ImmutableIJsonFileSparkMSourceProps.class)
public interface IJsonFileSparkMSourceProps extends IMSourceProps {
    public String path();
}

