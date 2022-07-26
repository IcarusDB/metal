package org.metal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableIMSourceProps.class)
@JsonDeserialize(as = ImmutableIMSourceProps.class)
public interface IMSourceProps extends IMetalProps {
    public String schema();
}
