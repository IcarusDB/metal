package org.metal.backend.spark.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.metal.core.props.IMSourceProps;

@Value.Immutable
@JsonDeserialize(as = ImmutableIJsonFileMSourceProps.class)
@JsonSerialize(as = ImmutableIJsonFileMSourceProps.class)
public interface IJsonFileMSourceProps extends IMSourceProps {
    public String path();
}

